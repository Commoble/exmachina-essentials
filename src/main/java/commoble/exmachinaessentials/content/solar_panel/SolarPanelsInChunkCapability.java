package commoble.exmachinaessentials.content.solar_panel;

import java.util.ArrayList;
import java.util.HashSet;

import commoble.exmachinaessentials.content.wire_post.PostsInChunkCapability;
import commoble.exmachinaessentials.util.NBTListCodec;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;

public class SolarPanelsInChunkCapability
{
	@CapabilityInject(ISolarPanelsInChunk.class)
	public static Capability<ISolarPanelsInChunk> INSTANCE = null;
	
	public static class Storage implements Capability.IStorage<ISolarPanelsInChunk>
	{		
		public static final NBTListCodec<BlockPos, CompoundNBT> POS_LISTER = PostsInChunkCapability.Storage.POS_LISTER;
		
		@Override
		public INBT writeNBT(Capability<ISolarPanelsInChunk> capability, ISolarPanelsInChunk instance, Direction side)
		{
			return POS_LISTER.write(new ArrayList<>(instance.getPositions()), new CompoundNBT());
		}

		@Override
		public void readNBT(Capability<ISolarPanelsInChunk> capability, ISolarPanelsInChunk instance, Direction side, INBT nbt)
		{
			if (nbt instanceof CompoundNBT)
			{
				instance.setPositions(new HashSet<>(POS_LISTER.read((CompoundNBT)nbt)));
			}
		}
		
	}
}
