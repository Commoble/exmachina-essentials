package commoble.exmachinaessentials.content;

import net.java.games.util.plugins.Plugin;
import net.minecraft.util.ResourceLocation;

@AutoPlugin
public class EssentialsPlugin implements Plugin
{
	@Override
	public void register(PluginRegistrator registry)
	{
		registry.registerConnectionType(new ResourceLocation("exmachina:wire_post"), WirePostBlock::getPotentialConnections);
		
	}

}
