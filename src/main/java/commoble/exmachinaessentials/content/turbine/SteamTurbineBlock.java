package commoble.exmachinaessentials.content.turbine;

import java.util.Random;

import commoble.exmachinaessentials.content.TileEntityRegistrar;
import commoble.exmachinaessentials.util.ItemHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.items.CapabilityItemHandler;

public class SteamTurbineBlock extends Block
{
	public static final BooleanProperty LIT = BlockStateProperties.LIT;
	public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
	
	public SteamTurbineBlock(Properties props)
	{
		super(props);
		this.setDefaultState(this.getStateContainer().getBaseState().with(LIT, false).with(FACING, Direction.NORTH));
	}

	@Override
	public boolean hasTileEntity(BlockState state)
	{
		return true;
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world)
	{
		return TileEntityRegistrar.STEAM_TURBINE.get().create();
	}

	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder)
	{
		builder.add(LIT, FACING);
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context)
	{
	      return this.getDefaultState().with(FACING, context.getPlacementHorizontalFacing().getOpposite());
	}

	@Override
	@Deprecated
	public ActionResultType onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult raytrace)
	{
		TileEntity te = world.getTileEntity(pos);
		if (te instanceof SteamTurbineTileEntity)
		{
			if (player instanceof ServerPlayerEntity)
			{
				ServerPlayerEntity serverPlayer = (ServerPlayerEntity)player;
				SteamTurbineTileEntity turbine = (SteamTurbineTileEntity)te;
				INamedContainerProvider provider = SteamTurbineContainer.getServerContainerProvider(turbine, pos);
				NetworkHooks.openGui(serverPlayer, provider);
			}
			
			return ActionResultType.SUCCESS; // always return same result on client/server
		}
		return super.onUse(state, world, pos, player, hand, raytrace);
	}
	
	@Override
	public BlockState rotate(BlockState state, Rotation rotation)
	{
		return state.with(FACING, rotation.rotate(state.get(FACING)));
	}
	
	@Override
	@Deprecated
	public BlockState mirror(BlockState state, Mirror mirror)
	{
		return state.rotate(mirror.toRotation(state.get(FACING)));
	}
	
	@Override
	@Deprecated
	public void onReplaced(BlockState thisOldState, World world, BlockPos pos, BlockState newState, boolean p_196243_5_)
	{
		// if we're changing to a different block entirely
		if (!thisOldState.isIn(newState.getBlock()))
		{
			TileEntity tileentity = world.getTileEntity(pos);
			if (tileentity instanceof SteamTurbineTileEntity)
			{
				((SteamTurbineTileEntity)tileentity).dropItems();
				world.updateComparatorOutputLevel(pos, this);
			}

			super.onReplaced(thisOldState, world, pos, newState, p_196243_5_);
		}
	}

	@Override
	@Deprecated
	public boolean hasComparatorInputOverride(BlockState p_149740_1_)
	{
		return true;
	}

	@SuppressWarnings("deprecation")
	@Override
	@Deprecated
	public int getComparatorInputOverride(BlockState state, World world, BlockPos pos)
	{
		TileEntity te = world.getTileEntity(pos);
		if (te instanceof SteamTurbineTileEntity)
		{
			return te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
				.map(ItemHelper::calcRedstoneFromItemHandler)
				.orElseGet(() -> super.getComparatorInputOverride(state, world, pos));
		}
		else
		{
			return super.getComparatorInputOverride(state, world, pos);
		}
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void animateTick(BlockState state, World world, BlockPos pos, Random rand)
	{
		if (state.get(LIT))
		{
			double xCenter = pos.getX() + 0.5D;
			double yBottom = pos.getY();
//			double yCenter = yBottom + 0.5D;
			double zCenter = pos.getZ() + 0.5D;
			if (rand.nextDouble() < 0.1D)
			{
				world.playSound(xCenter, yBottom, zCenter, SoundEvents.BLOCK_BLASTFURNACE_FIRE_CRACKLE, SoundCategory.BLOCKS, 1.0F, 1.0F, false);
			}

			// add fire particles
			Direction direction = state.get(FACING);
			Direction.Axis axis = direction.getAxis();
			double sideOff = rand.nextDouble() * 0.6D - 0.3D;
			double xOff = axis == Direction.Axis.X ? direction.getXOffset() * 0.52D : sideOff;
			double yOff = rand.nextDouble() * 9.0D / 16.0D;
			double zOff = axis == Direction.Axis.Z ? direction.getZOffset() * 0.52D : sideOff;
			world.addParticle(ParticleTypes.SMOKE, xCenter + xOff, yBottom + yOff, zCenter + zOff, 0.0D, 0.0D, 0.0D);
		}
	}
}
