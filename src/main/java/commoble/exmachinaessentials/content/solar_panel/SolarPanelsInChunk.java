package commoble.exmachinaessentials.content.solar_panel;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;

public class SolarPanelsInChunk implements ISolarPanelsInChunk, ICapabilityProvider, INBTSerializable<CompoundNBT>
{
	public final LazyOptional<ISolarPanelsInChunk> holder = LazyOptional.of(() -> this);
	
	private Set<BlockPos> positions = new HashSet<>();
	
	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side)
	{
		if (cap == SolarPanelsInChunkCapability.INSTANCE)
		{
			return SolarPanelsInChunkCapability.INSTANCE.orEmpty(cap, this.holder);
		}
		else
		{
			return LazyOptional.empty();
		}
	}

	@Override
	public Set<BlockPos> getPositions()
	{
		return this.positions;
	}

	@Override
	public void setPositions(Set<BlockPos> set)
	{
		this.positions = set;
	}

	@Override
	public CompoundNBT serializeNBT()
	{
		return (CompoundNBT)SolarPanelsInChunkCapability.INSTANCE.getStorage().writeNBT(SolarPanelsInChunkCapability.INSTANCE, this, null);
	}

	@Override
	public void deserializeNBT(CompoundNBT nbt)
	{
		SolarPanelsInChunkCapability.INSTANCE.getStorage().readNBT(SolarPanelsInChunkCapability.INSTANCE, this, null, nbt);
	}

}
