package commoble.exmachinaessentials.content;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.items.ItemStackHandler;

public class FuelItemHandler extends ItemStackHandler
{
	public final TileEntity te;
	
	public FuelItemHandler(int size, TileEntity te)
	{
		super(size);
		this.te = te;
	}
	
	@Override
	public boolean isItemValid(int slot, ItemStack stack)
	{
		return ForgeHooks.getBurnTime(stack) > 0;
	}
	
	@Override
	protected void onContentsChanged(int slot)
	{
		super.onContentsChanged(slot);
		this.te.markDirty();
	}
}
