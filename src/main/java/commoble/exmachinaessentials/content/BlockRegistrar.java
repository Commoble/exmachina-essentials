package commoble.exmachinaessentials.content;

import java.util.function.Function;

import commoble.exmachinaessentials.ExMachinaEssentials;
import commoble.exmachinaessentials.Names;
import commoble.exmachinaessentials.content.solar_panel.SolarPanelBlock;
import commoble.exmachinaessentials.content.wire_post.WirePostBlock;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class BlockRegistrar
{
	public static final DeferredRegister<Block> BLOCKS = ExMachinaEssentials.createDeferredRegister(ForgeRegistries.BLOCKS);
	
	public static final RegistryObject<WirePostBlock> WIRE_POST = registerBlock(Names.WIRE_POST, WirePostBlock::new, AbstractBlock.Properties.from(Blocks.PISTON));
	public static final RegistryObject<SolarPanelBlock> SOLAR_PANEL = registerBlock(Names.SOLAR_PANEL, SolarPanelBlock::new, AbstractBlock.Properties.create(Material.ROCK).hardnessAndResistance(0.2F).sound(SoundType.STONE));
//	public static final RegistryObject<Block> ELECTRIC_FURNACE = BLOCKS.register(Names.ELECTRIC_FURNACE, () -> new Block(AbstractBlock.Properties.from(Blocks.SMITHING_TABLE)));

	public static final <BLOCK extends Block> RegistryObject<BLOCK> registerBlock(String name, Function<AbstractBlock.Properties, BLOCK> constructor, AbstractBlock.Properties props)
	{
		return BLOCKS.register(name, () -> constructor.apply(props));
	}
}
