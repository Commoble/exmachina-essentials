package commoble.exmachinaessentials.client;

import commoble.exmachinaessentials.content.BlockRegistrar;
import commoble.exmachinaessentials.content.ContainerRegistrar;
import commoble.exmachinaessentials.content.TileEntityRegistrar;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class ClientEvents
{
	// called from mod constructor if on physical client
	public static void subscribeClientEvents(IEventBus modBus, IEventBus forgeBus)
	{
		modBus.addListener(ClientEvents::onClientSetup);
	}
	
	public static void onClientSetup(FMLClientSetupEvent event)
	{
		// block render types
		RenderTypeLookup.setRenderLayer(BlockRegistrar.STEAM_TURBINE.get(), RenderType.getCutout());
		
		// TERs
		ClientRegistry.bindTileEntityRenderer(TileEntityRegistrar.WIRE_POST.get(), WirePostRenderer::new);
		
		// screen factories
		ScreenManager.registerFactory(ContainerRegistrar.STEAM_TURBINE.get(), SteamTurbineScreen::new);
	}
}
