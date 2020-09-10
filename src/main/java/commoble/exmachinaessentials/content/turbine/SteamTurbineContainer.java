package commoble.exmachinaessentials.content.turbine;

import commoble.exmachinaessentials.content.BlockRegistrar;
import commoble.exmachinaessentials.content.ContainerRegistrar;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIntArray;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.util.IntArray;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;

public class SteamTurbineContainer extends Container
{
	public static final ITextComponent TITLE = new TranslationTextComponent("container.exmachinaessentials.steam_turbine");
	
	// slot counts
	private static final int FUEL_SLOT = 0;
	private static final int FIRST_PLAYER_SLOT = 1;
	private static final int BACKPACK_COLUMNS = 9;
	private static final int BACKPACK_ROWS = 3;
	private static final int TOTAL_PLAYER_SLOTS = BACKPACK_COLUMNS * (BACKPACK_ROWS+1);
	private static final int TOTAL_SLOTS = TOTAL_PLAYER_SLOTS+1;
	
	// slot positioning
	private static final int SLOT_SPACING = 18;
	private static final int FUEL_X = 56;
	private static final int FUEL_Y = 53;
	private static final int BACKPACK_START_X = 8;
	private static final int BACKPACK_START_Y = 84;
	private static final int HOTBAR_START_Y = 142;
	
	private final IWorldPosCallable usabilityTest;
	private final IIntArray data;
	
	public static SteamTurbineContainer createClientContainer(int id, PlayerInventory playerInventory)
	{
		return new SteamTurbineContainer(id, playerInventory, BlockPos.ZERO, new ItemStackHandler(1), new IntArray(2));
	}
	
	public static INamedContainerProvider getServerContainerProvider(SteamTurbineTileEntity te, BlockPos pos)
	{
		return new SimpleNamedContainerProvider((id, playerInventory, player) -> new SteamTurbineContainer(id, playerInventory, pos, te.fuel, new SyncData(te)), TITLE);
	}
	
	protected SteamTurbineContainer(int id, PlayerInventory playerInventory, BlockPos pos, IItemHandler fuel, IIntArray data)
	{
		super(ContainerRegistrar.STEAM_TURBINE.get(), id);
		this.usabilityTest = IWorldPosCallable.of(playerInventory.player.world, pos);
		this.data = data;
		this.trackIntArray(this.data);
		
		// add fuel slot
		this.addSlot(new SlotItemHandler(fuel, 0, FUEL_X, FUEL_Y));
		
		// add hotbar slots
		for (int hotbarSlot = 0; hotbarSlot < BACKPACK_COLUMNS; hotbarSlot++)
		{
			int x = BACKPACK_START_X + SLOT_SPACING * hotbarSlot;
			this.addSlot(new Slot(playerInventory, hotbarSlot, x, HOTBAR_START_Y));
		}
		// add backpack slots
		for (int row=0; row < BACKPACK_ROWS; row++)
		{
			int y = BACKPACK_START_Y + SLOT_SPACING*row;
			for (int column=0; column < BACKPACK_COLUMNS; column++)
			{
				int x = BACKPACK_START_X + SLOT_SPACING*column;
				int index = row*BACKPACK_COLUMNS + column + BACKPACK_COLUMNS; // use same sequence of indexes as hotbar slots
				this.addSlot(new Slot(playerInventory, index, x, y));
			}
		}
	}

	@Override
	public boolean canInteractWith(PlayerEntity player)
	{
		return Container.isWithinUsableDistance(this.usabilityTest, player, BlockRegistrar.STEAM_TURBINE.get());
	}

	// called when the player shift-clicks a slot to move a whole stack to a different side of the inventory
	// slot indices here are the indices in the container
	// (as opposed to the slot indices they were created with, which are their relative indices in the inventory they are assigned to)
	@Override
	public ItemStack transferStackInSlot(PlayerEntity player, int index)
	{
		ItemStack slotStackCopy = ItemStack.EMPTY;
		Slot slot = this.inventorySlots.get(index);
		
		if (slot != null && slot.getHasStack())
		{
			ItemStack stackInSlot = slot.getStack();
			slotStackCopy = stackInSlot.copy();
			// if this is a fuel slot, try to move it to player inventory
			if (index == FUEL_SLOT)
			{
				if (!this.mergeItemStack(stackInSlot, FIRST_PLAYER_SLOT, TOTAL_SLOTS, false))
				{
					return ItemStack.EMPTY;
				}
			}
			// otherwise if this is a player slot
			else
			{
				// if we can burn the item, try to put it in the fuel slots first
				if (ForgeHooks.getBurnTime(stackInSlot) > 0 && !this.mergeItemStack(stackInSlot, FUEL_SLOT, FIRST_PLAYER_SLOT, false))
				{
					return ItemStack.EMPTY;
				}
			}
			
			if (stackInSlot.isEmpty())
			{
				slot.putStack(ItemStack.EMPTY);
			}
			else
			{
				slot.onSlotChanged();
			}
			
			if (stackInSlot.getCount() == slotStackCopy.getCount())
			{
				return ItemStack.EMPTY;
			}
			
			slot.onTake(player, stackInSlot);
		}
		
		return slotStackCopy;
	}
	
	public int getBurnTimeRemaining()
	{
		return this.data.get(0);
	}
	
	public int getLastBurnValue()
	{
		return this.data.get(1);
	}

	public int getBurnLeftScaled()
	{
		int totalBurnTime = this.getLastBurnValue();
		if (totalBurnTime == 0)
		{
			totalBurnTime = 200;
		}

		return this.getBurnTimeRemaining() * 13 / totalBurnTime;
	}
	
	static class SyncData implements IIntArray
	{
		private final SteamTurbineTileEntity te;
		
		public SyncData(SteamTurbineTileEntity te)
		{
			this.te = te;
		}

		@Override
		public int get(int i)
		{
			switch (i)
			{
				case 0:
					return this.te.burnTimeRemaining;
				case 1:
					return this.te.lastItemBurnedValue;
				default:
					return 0;
			}
		}

		@Override
		public void set(int i, int value)
		{
			switch (i)
			{
				case 0:
					this.te.burnTimeRemaining = value;
					break;
				case 1:
					this.te.lastItemBurnedValue = value;
					break;
				default:
					break;
			}
		}

		@Override
		public int size()
		{
			return 2;
		}
		
	}
}
