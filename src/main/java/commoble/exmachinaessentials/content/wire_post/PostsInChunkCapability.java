package commoble.exmachinaessentials.content.wire_post;

import java.util.ArrayList;
import java.util.HashSet;

import commoble.exmachinaessentials.util.NBTListCodec;
import commoble.exmachinaessentials.util.NBTListCodec.ListNBTType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;

public class PostsInChunkCapability
{
	@CapabilityInject(IPostsInChunk.class)
	public static Capability<IPostsInChunk> INSTANCE = null;
	
	public static class Storage implements Capability.IStorage<IPostsInChunk>
	{
		public static final String POSITIONS = "positions";
		
		public static final NBTListCodec<BlockPos, CompoundNBT> POS_LISTER = new NBTListCodec<>(
			POSITIONS,
			ListNBTType.COMPOUND,
			NBTUtil::writeBlockPos,
			NBTUtil::readBlockPos
			);
		
		@Override
		public INBT writeNBT(Capability<IPostsInChunk> capability, IPostsInChunk instance, Direction side)
		{
			return POS_LISTER.write(new ArrayList<>(instance.getPositions()), new CompoundNBT());
		}

		@Override
		public void readNBT(Capability<IPostsInChunk> capability, IPostsInChunk instance, Direction side, INBT nbt)
		{
			if (nbt instanceof CompoundNBT)
			{
				instance.setPositions(new HashSet<>(POS_LISTER.read((CompoundNBT)nbt)));
			}
		}
		
	}
}
