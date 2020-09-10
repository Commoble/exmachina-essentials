package commoble.exmachinaessentials.content;

import commoble.exmachinaessentials.ExMachinaEssentials;
import commoble.exmachinaessentials.Names;
import commoble.exmachinaessentials.content.turbine.SteamTurbineContainer;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ContainerRegistrar
{
	public static final DeferredRegister<ContainerType<?>> TYPES = DeferredRegister.create(ForgeRegistries.CONTAINERS, ExMachinaEssentials.MODID);
	
	public static final RegistryObject<ContainerType<SteamTurbineContainer>> STEAM_TURBINE = register(Names.STEAM_TURBINE, SteamTurbineContainer::createClientContainer);
		
	public static <CONTAINER extends Container> RegistryObject<ContainerType<CONTAINER>>
		register(String name, ContainerType.IFactory<CONTAINER> clientFactory)
	{
		return TYPES.register(name, () -> new ContainerType<>(clientFactory));
	}
}
