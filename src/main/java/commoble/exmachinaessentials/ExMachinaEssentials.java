package commoble.exmachinaessentials;

import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;

import commoble.exmachinaessentials.client.ClientEvents;
import commoble.exmachinaessentials.content.BlockRegistrar;
import commoble.exmachinaessentials.content.ItemRegistrar;
import commoble.exmachinaessentials.content.TileEntityRegistrar;
import commoble.exmachinaessentials.content.wire_post.IPostsInChunk;
import commoble.exmachinaessentials.content.wire_post.PostsInChunk;
import commoble.exmachinaessentials.content.wire_post.PostsInChunkCapability;
import commoble.exmachinaessentials.content.wire_post.WireBreakPacket;
import commoble.exmachinaessentials.content.wire_post.WirePostTileEntity;
import commoble.exmachinaessentials.util.ConfigHelper;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.network.play.server.SEntityEquipmentPacket;
import net.minecraft.network.play.server.SPlaySoundEffectPacket;
import net.minecraft.particles.RedstoneParticleData;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig.Type;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;

@Mod(ExMachinaEssentials.MODID)
public class ExMachinaEssentials
{
	public static final String MODID = "exmachinaessentials";
	public static final Logger LOGGER = LogManager.getLogger();
	
	public static ExMachinaEssentials INSTANCE;
	
	// the network channel we'll use for sending packets associated with this mod
	public static final String CHANNEL_PROTOCOL_VERSION = "1";
	public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
		new ResourceLocation(ExMachinaEssentials.MODID, "main"),
		() -> CHANNEL_PROTOCOL_VERSION,
		CHANNEL_PROTOCOL_VERSION::equals,
		CHANNEL_PROTOCOL_VERSION::equals);
	
	public final ServerConfig serverConfig;
	
	// forge constructs this during modloading
	public ExMachinaEssentials()
	{
		INSTANCE = this;
		
		IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
		IEventBus forgeBus = MinecraftForge.EVENT_BUS;

		this.serverConfig = ConfigHelper.register(Type.SERVER, ServerConfig::new);
		
		// subscribe events to mod bus -- registries and other init events, mostly
		// subscribe deferred registers so they register our stuff for us
		DeferredRegister<?>[] registers =
		{
			BlockRegistrar.BLOCKS,
			ItemRegistrar.ITEMS,
			TileEntityRegistrar.TYPES
		};
		
		for (DeferredRegister<?> register : registers)
		{
			register.register(modBus);
		}
		
		// subscribe the rest of the mod event listeners
		modBus.addListener(this::onCommonSetup);
		
		// subscribe events to forge bus -- server init and in-game events
		forgeBus.addGenericListener(Chunk.class, this::onAttachChunkCapabilities);
		forgeBus.addListener(EventPriority.LOW, this::checkBlockingWiresOnEntityPlaceBlock);
		
		// subscribe to client events separately so they don't break servers
		if (FMLEnvironment.dist == Dist.CLIENT)
		{
			ClientEvents.subscribeClientEvents(modBus, forgeBus);
		}
		
	}

	private void onCommonSetup(FMLCommonSetupEvent event)
	{
		// register packets
		int packetID = 0;
		CHANNEL.registerMessage(packetID++,
			WireBreakPacket.class,
			WireBreakPacket::write,
			WireBreakPacket::read,
			WireBreakPacket::handle);
		
		// register capabilities
		CapabilityManager.INSTANCE.register(IPostsInChunk.class, new PostsInChunkCapability.Storage(), PostsInChunk::new);
	}
	
	private void onAttachChunkCapabilities(AttachCapabilitiesEvent<Chunk> event)
	{
		PostsInChunk postsInChunk = new PostsInChunk();
		event.addCapability(getModRL(Names.POSTS_IN_CHUNK), postsInChunk);
		event.addListener(() -> postsInChunk.holder.invalidate());
		
	}
	
	private void checkBlockingWiresOnEntityPlaceBlock(BlockEvent.EntityPlaceEvent event)
	{
		BlockPos pos = event.getPos();
		IWorld iworld = event.getWorld();
		BlockState state = event.getState();
		if (iworld instanceof World && !iworld.isRemote())
		{
			World world = (World)iworld;
			
			Set<ChunkPos> chunkPositions = PostsInChunk.getRelevantChunkPositionsNearPos(pos);
			
			for (ChunkPos chunkPos : chunkPositions)
			{
				if (world.isBlockLoaded(chunkPos.asBlockPos()))
				{
					Chunk chunk = world.getChunk(chunkPos.x, chunkPos.z);
					chunk.getCapability(PostsInChunkCapability.INSTANCE).ifPresent(posts ->
					{
						Set<BlockPos> checkedPostPositions = new HashSet<BlockPos>();
						for (BlockPos postPos : posts.getPositions())
						{
							TileEntity te = world.getTileEntity(postPos);
							if (te instanceof WirePostTileEntity)
							{
								Vector3d hit = ((WirePostTileEntity)te).doesBlockStateIntersectConnection(pos, state, checkedPostPositions);
								if (hit != null)
								{
									event.setCanceled(true);
									Entity entity = event.getEntity();
									if (entity instanceof ServerPlayerEntity)
									{
										ServerPlayerEntity serverPlayer = (ServerPlayerEntity)entity;
										serverPlayer.connection.sendPacket(new SEntityEquipmentPacket(serverPlayer.getEntityId(), Lists.newArrayList(Pair.of(EquipmentSlotType.MAINHAND, serverPlayer.getHeldItem(Hand.MAIN_HAND)))));
										((ServerWorld)world).spawnParticle(serverPlayer, RedstoneParticleData.REDSTONE_DUST, false, hit.x, hit.y, hit.z, 5, .05, .05, .05, 0);
										serverPlayer.connection.sendPacket(new SPlaySoundEffectPacket(SoundEvents.ENTITY_WANDERING_TRADER_HURT, SoundCategory.BLOCKS, hit.x, hit.y, hit.z, 0.5F, 2F));
									}
									return;
								}
								else
								{
									checkedPostPositions.add(postPos);
								}
							}
						}
					});
				}
			}
		}
	}
	
	public static ResourceLocation getModRL(String name)
	{
		return new ResourceLocation(MODID, name);
	}
	
	public static <T extends IForgeRegistryEntry<T>> DeferredRegister<T> createDeferredRegister(IForgeRegistry<T> registry)
	{
		return DeferredRegister.create(registry, ExMachinaEssentials.MODID);
	}
}
