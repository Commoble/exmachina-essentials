package commoble.exmachinaessentials.content;

import java.util.function.Supplier;

import commoble.exmachinaessentials.ExMachinaEssentials;
import commoble.exmachinaessentials.Names;
import commoble.exmachinaessentials.content.wire_post.WireSpoolItem;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ItemRegistrar
{
	public static final DeferredRegister<Item> ITEMS = ExMachinaEssentials.createDeferredRegister(ForgeRegistries.ITEMS);
	
	public static final ItemGroup CREATIVE_TAB = new ItemGroup(ExMachinaEssentials.MODID)
	{
		@Override
		public ItemStack createIcon()
		{
			return new ItemStack(Items.ACACIA_BOAT);
		}
	};
	
	// items we don't need references to
	static
	{
		registerBlockItem(Names.WIRE_POST, BlockRegistrar.WIRE_POST);
		registerBlockItem(Names.SOLAR_PANEL, BlockRegistrar.SOLAR_PANEL);
		registerBlockItem(Names.BATTERY_BOX, BlockRegistrar.BATTERY_BOX);
		registerBlockItem(Names.STEAM_TURBINE, BlockRegistrar.STEAM_TURBINE);
		
		ITEMS.register(Names.MONDOMETER, () -> new MondometerItem(new Item.Properties().group(CREATIVE_TAB)));
		ITEMS.register(Names.WIRE_SPOOL, () -> new WireSpoolItem(new Item.Properties().group(CREATIVE_TAB)));
		
	}
	
	public static RegistryObject<BlockItem> registerBlockItem(String name, Supplier<? extends Block> blockGetter)
	{
		return ITEMS.register(name, () -> new BlockItem(blockGetter.get(), new Item.Properties().group(CREATIVE_TAB)));
	}
}
