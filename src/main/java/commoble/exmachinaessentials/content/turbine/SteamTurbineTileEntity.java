package commoble.exmachinaessentials.content.turbine;

import commoble.exmachinaessentials.content.FuelItemHandler;
import commoble.exmachinaessentials.content.TileEntityRegistrar;
import commoble.exmachinaessentials.util.ItemHelper;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

public class SteamTurbineTileEntity extends TileEntity implements ITickableTileEntity
{
	// NBT keys
	public static final String FUEL = "fuel";
	public static final String BURN_TIME = "burn_time";
	public static final String BURN_VALUE = "burn_value";
	
	public final ItemStackHandler fuel = new FuelItemHandler(1, this);
	public final LazyOptional<IItemHandler> fuelOptional = LazyOptional.of(() -> this.fuel);
	
	public int burnTimeRemaining = 0;
	public int lastItemBurnedValue = 200;

	public SteamTurbineTileEntity()
	{
		super(TileEntityRegistrar.STEAM_TURBINE.get());
	}

	@Override
	public void fromTag(BlockState state, CompoundNBT nbt)
	{
		super.fromTag(state, nbt);
		this.fuel.deserializeNBT(nbt.getCompound(FUEL));
		this.burnTimeRemaining = nbt.getInt(BURN_TIME);
		this.lastItemBurnedValue = nbt.getInt(BURN_VALUE);
	}

	@Override
	public CompoundNBT write(CompoundNBT nbt)
	{
		super.write(nbt);
		nbt.put(FUEL, this.fuel.serializeNBT());
		nbt.putInt(BURN_TIME, this.burnTimeRemaining);
		nbt.putInt(BURN_VALUE, this.lastItemBurnedValue);
		return nbt;
	}

	@Override
	protected void invalidateCaps()
	{
		super.invalidateCaps();
		this.fuelOptional.invalidate();
	}

	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side)
	{
		if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
		{
			return this.fuelOptional.cast();
		}
		return super.getCapability(cap, side);
	}

	@Override
	public void tick()
	{
		// furnace-like logic
		// if burning, decrement burn time
		boolean dirty = false;
		boolean wasBurningBeforeTick = this.isBurning();
		if (wasBurningBeforeTick)
		{
			this.burnTimeRemaining--;
			dirty = true;
		}
		
		if (!this.world.isRemote())
		{
			// if burning, or can start burning fuel and has a burnable input
			if (this.isBurning() || this.canConsumeFuel())
			{
				// if not burning but can start burning
				if (!this.isBurning())
				{
					// consume fuel and start burning
					// set burn time based on fuel input
					this.consumeFuel();
					// if burning
						// consume fuel, replacing with container item if consumed fuel item has container item
				}
				// if burning and has smeltable input
				if (this.isBurning())
				{
					// increase cook progress
					// if cook progress is complete
						// reset cook progress
						// craft ingredients into result
				}
				// otherwise
					// reset cookprogress
			}
			// otherwise, if not burning but has cook progress
				// reduce cook progress
			
			boolean isBurningAfterTick = this.isBurning();
			// if burning state changed since tick started
			if (isBurningAfterTick != wasBurningBeforeTick)
			{
				// set furnace's litness state
				this.updateLitnessState(isBurningAfterTick);
			}
			// if any relevant data changed
			if (dirty)
			{
				this.markDirty();
			}
		}
		// play sounds on the client (TE ticks more regularly than block)
		else
		{
			// TODO the sound hurts my ears
//			BlockState state = this.getBlockState();
//			if (state.get(SteamTurbineBlock.LIT))
//			{
//				if (this.world.getGameTime() % 2 == 0)
//				{
//					double x = this.pos.getX() + 0.5D;
//					double y = this.pos.getY() + 0.5D;
//					double z = this.pos.getZ() + 0.5D;
//					this.world.playSound(x, y, z, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS,
//						0.99F + this.world.rand.nextFloat() * 0.01F,
//						1.9F + this.world.rand.nextFloat() * 0.1F,
//						false);
//				}
//			}
		}
	}
	
	public void updateLitnessState(boolean isBurning)
	{
		this.world.setBlockState(this.pos, this.getBlockState().with(SteamTurbineBlock.LIT, isBurning));
	}

	public boolean isBurning()
	{
		return this.burnTimeRemaining > 0;
	}
	
	public boolean canConsumeFuel()
	{
		int slots = this.fuel.getSlots();
		for (int slot=0; slot < slots; slot++)
		{
			ItemStack stackInSlot = this.fuel.getStackInSlot(slot);
			if (this.canBurnStack(stackInSlot))
			{
				return true;
			}
		}
		return false;
	}
	
	public boolean canBurnStack(ItemStack stack)
	{
		// for an item to be burnable, A) it must have a defined burn value, and
		// B) the stack cannot have both a container item AND multiple items in the stack
		return ForgeHooks.getBurnTime(stack) > 0 &&
			!(stack.hasContainerItem() && stack.getCount() > 1);
	}
	
	public void consumeFuel()
	{
		int slots = this.fuel.getSlots();
		for (int slot=0; slot < slots; slot++)
		{
			ItemStack stackToVerify = this.fuel.extractItem(slot, 1, true);
			if (this.canBurnStack(stackToVerify))
			{
				ItemStack extractedStack = this.fuel.extractItem(slot, 1, false);
				int burnTime = ForgeHooks.getBurnTime(extractedStack);
				this.burnTimeRemaining += burnTime;
				this.lastItemBurnedValue = burnTime;
				if (extractedStack.getCount() > 0 && this.fuel.getStackInSlot(slot).isEmpty())
				{	// if we burned the last of the fuel, set container item
					this.fuel.setStackInSlot(slot, extractedStack.getContainerItem());
				}
				this.markDirty();
				break;
			}
		}
	}
	
	public void dropItems()
	{
		ItemHelper.dropItems(this.world, this.pos, this.fuel);
	}
}
