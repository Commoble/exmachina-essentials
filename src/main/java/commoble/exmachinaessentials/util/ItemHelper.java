package commoble.exmachinaessentials.util;

import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.ObjIntConsumer;
import java.util.function.Predicate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.base.Predicates;

import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandler;

public class ItemHelper
{
	public static void forEachSlotInHandler(IItemHandler handler, ObjIntConsumer<IItemHandler> consumer)
	{
		int slots = handler.getSlots();
		for (int i=0; i < slots; i++)
		{
			consumer.accept(handler, i);
		}
	}
	
	/**
	 * 
	 * @param <T> Type to reduce list of items to
	 * @param handler An IItemHandler with a finite number of slots
	 * @param base Output object to begin the accumulator at. This will be returned as-is if the handler has no slots.
	 * @param filter Filter to apply to itemstacks. Items that do not pass the filter will not be mapped and accumulated.
	 * @param mapper mapper
	 * @param combiner A binary operator that applies the mapped itemstacks to the accumulator. Accumulator will be the left-hand operand.
	 * @return reduced result
	 */
	public static <T> T reduceItemHandler(IItemHandler handler, T base, Predicate<ItemStack> filter, Function<ItemStack, T> mapper, BinaryOperator<T> combiner)
	{
		int slots = handler.getSlots();
		T result = base;
		for (int i=0; i < slots; i++)
		{
			ItemStack stack = handler.getStackInSlot(i);
			result = combiner.apply(base, mapper.apply(stack));
		}
		
		return result;
	}
	
	public static void dropItems(World world, BlockPos fromPos, IItemHandler handler)
	{
		ItemHelper.forEachSlotInHandler(handler, (theHandler, i) -> ItemHelper.ejectItemstack(world, fromPos, null, theHandler.extractItem(i, Integer.MAX_VALUE, false)));
	}
	
	public static void ejectItemstack(World world, BlockPos fromPos, @Nullable Direction outputDir, ItemStack stack)
	{
		// if there is room in front of the block, eject items there
		double x, y, z, xVel, yVel, zVel, xOff, yOff, zOff;
		BlockPos output_pos;
		if (outputDir != null)
		{
			output_pos = fromPos.offset(outputDir);
			xOff = outputDir.getXOffset();
			yOff = outputDir.getYOffset();
			zOff = outputDir.getZOffset();
		}
		else
		{
			output_pos = fromPos;
			xOff = 0D;
			yOff = 0D;
			zOff = 0D;
		}
		if (!world.getBlockState(output_pos).isSolid())
		{
			x = fromPos.getX() + 0.5D + xOff * 0.75D;
			y = fromPos.getY() + 0.25D + yOff * 0.75D;
			z = fromPos.getZ() + 0.5D + zOff * 0.75D;
			xVel = xOff * 0.1D;
			yVel = yOff * 0.1D;
			zVel = zOff * 0.1D;
		}
		else // otherwise just eject items inside the shunt
		{
			x = fromPos.getX() + 0.5D;
			y = fromPos.getY() + 0.5D;
			z = fromPos.getZ() + 0.5D;
			xVel = 0D;
			yVel = 0D;
			zVel = 0D;
		}
		ItemEntity itementity = new ItemEntity(world, x, y, z, stack);
		itementity.setDefaultPickupDelay();
		itementity.setMotion(xVel, yVel, zVel);
		world.addEntity(itementity);
	}
	
	public static final Predicate<ItemStack> NOT_EMPTY = Predicates.not(ItemStack::isEmpty);
	public static int calcRedstoneFromItemHandler(@Nonnull IItemHandler handler)
	{
		int itemCount = 0;	// number of non-empty itemstacks
		float cumulativePercentage = 0.0F;
		int slots = handler.getSlots();
		for (int i = 0; i < slots; ++i)
		{
			ItemStack stack = handler.getStackInSlot(i);
			if (!stack.isEmpty())
			{
				cumulativePercentage += stack.getCount() / (float) Math.min(handler.getSlotLimit(i), stack.getMaxStackSize());
				++itemCount;
			}
		}

		float averagePercentage = cumulativePercentage / slots;
		return MathHelper.floor(averagePercentage * 14.0F) + (itemCount > 0 ? 1 : 0);
		
	}
}
