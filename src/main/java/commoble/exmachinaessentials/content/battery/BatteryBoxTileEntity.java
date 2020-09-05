package commoble.exmachinaessentials.content.battery;

import commoble.exmachina.api.Circuit;
import commoble.exmachina.api.CircuitManager;
import commoble.exmachina.api.CircuitManagerCapability;
import commoble.exmachinaessentials.ExMachinaEssentials;
import commoble.exmachinaessentials.content.TileEntityRegistrar;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.LazyOptional;

public class BatteryBoxTileEntity extends TileEntity implements ITickableTileEntity
{
	private static final String ENERGY = "energy";
	
	private double storedEnergy;
	private LazyOptional<CircuitManager> managerHolder;

	public BatteryBoxTileEntity()
	{
		super(TileEntityRegistrar.BATTERY_BOX.get());
	}
	
	/**
	 * @return The total energy stored within, in Joules or Watt-seconds
	 */
	public double getStoredEnergy()
	{
		return this.storedEnergy;
	}

	/**
	 * @return The percentage of energy stored within relative to maximum capacity, in the range [0.0, 1.0]
	 */
	public double getRelativeStoredEnergy()
	{
		return this.storedEnergy / ExMachinaEssentials.INSTANCE.serverConfig.max_battery_box_energy.get();
	}
	
	@Override
	public void setLocation(World world, BlockPos pos)
	{
		super.setLocation(world, pos);
		this.managerHolder = world.getCapability(CircuitManagerCapability.INSTANCE);
	}
	
	private void writeToNBT(CompoundNBT nbt)
	{
		nbt.putDouble(ENERGY, this.storedEnergy);
	}
	
	private void readFromNBT(CompoundNBT nbt)
	{
		this.storedEnergy = nbt.getDouble(ENERGY);
	}

	/** Called when block entity is read from NBT **/
	@Override
	public void fromTag(BlockState state, CompoundNBT nbt)
	{
		super.fromTag(state, nbt);
		this.readFromNBT(nbt);
	}

	/** Called when block entity is written to NBT **/
	@Override
	public CompoundNBT write(CompoundNBT nbt)
	{
		super.write(nbt);
		this.writeToNBT(nbt);
		return nbt;
	}

	/** Called on the server when a client loads the chunk that contains this block entity**/
	@Override
	public CompoundNBT getUpdateTag()
	{
		CompoundNBT nbt = super.getUpdateTag();
		this.writeToNBT(nbt);
		return nbt;
	}

	/** Called on the server when a block update occurs that requires update packets be sent to clients **/
	@Override
	public SUpdateTileEntityPacket getUpdatePacket()
	{
		// the magic number here should be 0 for non-vanilla block entities
		return new SUpdateTileEntityPacket(this.pos, 0, this.write(new CompoundNBT()));
	}

	/** Called on the client when it receives an update packet from getUpdateTag or getUpdatePacket **/
	@Override
	public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt)
	{
		super.onDataPacket(net, pkt);
		this.readFromNBT(pkt.getNbtCompound());
	}

	@Override
	public void tick()
	{
		if (!this.world.isRemote)
		{
			this.managerHolder.ifPresent(this::tickCircuitManager);
			System.out.println(this.storedEnergy);
		}
	}
	
	private void tickCircuitManager(CircuitManager manager)
	{
		manager.getCircuit(this.pos).ifPresent(this::tickCircuit);
	}

	private void tickCircuit(Circuit circuit)
	{
		double oldEnergy = this.storedEnergy;
		double power = circuit.getPowerSuppliedTo(this.pos);
		double gainedEnergy = power * 0.05D;	// power = energy/time, energy change this tick = power * tick duration
		double newEnergy = MathHelper.clamp(oldEnergy + gainedEnergy, 0D, ExMachinaEssentials.INSTANCE.serverConfig.max_battery_box_energy.get());
		if (newEnergy != oldEnergy)
		{
			this.storedEnergy = newEnergy;
			circuit.markNeedingDynamicUpdate();
			BlockState state = this.getBlockState();
			this.markDirty();
			this.world.notifyBlockUpdate(this.pos, state, state, Constants.BlockFlags.DEFAULT);
		}
	}
	
}
