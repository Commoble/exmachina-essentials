package commoble.exmachinaessentials.content.battery;

import commoble.exmachinaessentials.content.TileEntityRegistrar;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

public class BatteryBoxBlock extends Block
{

	public BatteryBoxBlock(Properties props)
	{
		super(props);
	}

	@Override
	public boolean hasTileEntity(BlockState state)
	{
		return true;
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world)
	{
		return TileEntityRegistrar.BATTERY_BOX.get().create();
	}

	/**
	 * https://www.wolframalpha.com/input/?i=plot+y%3D1+-+%28%281%2F%28f%5E2%29%29+*+%28x-f%29%5E2%29%2C+x+from+0+to+0.2%2C+f+%3D+0.2
	 * @param world world
	 * @param pos pos
	 * @param state state
	 * @param max Maximum output value; value when energy is above falloff percentage
	 * @param falloff Positive (nonzero) percentage of current stored energy (relative to total capacity) below which output decreases to 0
	 * @return A value in the range [0,max] when current energy percentage is in the range [0,falloff], and max when above falloff
	 */
	public double getRisingAttenuation(IWorld world, BlockPos pos, BlockState state, double max, double falloff)
	{
		TileEntity te = world.getTileEntity(pos);
		if (te instanceof BatteryBoxTileEntity)
		{
			double energyPercentage = ((BatteryBoxTileEntity)te).getRelativeStoredEnergy();
			if (energyPercentage > falloff)
			{
				return max;
			}
			else
			{
				double sqFalloffDivisor = 1/(falloff*falloff);
				double percentageDiff = energyPercentage - falloff;
				double sqPercentageDiff = percentageDiff*percentageDiff;
				// 1 - ((p-f)^2 / f^2)
				// this gives us a nice upside-down parabola where the output is 0 when energyPercentage is 0
				// and the output is 1 when energyPercentage == falloff
				return (1D - (sqPercentageDiff * sqFalloffDivisor)) * max;
			}
		}
		else
		{
			return 0D;
		}
	}
	/**
	 * @param world world
	 * @param pos pos
	 * @param state state
	 * @param charging The value to return while the battery is gaining energy
	 * @param discharging The value to return while the battery is losing energy
	 * @return Either charging or discharging, depending on the battery's state
	 */
	public double getInvertableChargerValue(IWorld world, BlockPos pos, BlockState state, double charging, double discharging, double breakpoint)
	{
		TileEntity te = world.getTileEntity(pos);
		if (te instanceof BatteryBoxTileEntity)
		{
			return ((BatteryBoxTileEntity)te).getIsCharging(breakpoint) ? charging : discharging;
		}
		else
		{
			return charging; // reasoning: return what we would have given a battery with no charge
		}
	}
	
	@Override
	@Deprecated
	public boolean hasComparatorInputOverride(BlockState p_149740_1_)
	{
		return true;
	}

	@Override
	@Deprecated
	public int getComparatorInputOverride(BlockState state, World world, BlockPos pos)
	{
		TileEntity te = world.getTileEntity(pos);
		if (te instanceof BatteryBoxTileEntity)
		{
			return (int) (((BatteryBoxTileEntity)te).getRelativeStoredEnergy() * 15);
		}
		else
		{
			return super.getComparatorInputOverride(state, world, pos);
		}
	}
}
