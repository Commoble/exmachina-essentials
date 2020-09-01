package commoble.exmachinaessentials.content;

import commoble.exmachina.api.AutoPlugin;
import commoble.exmachina.api.Plugin;
import commoble.exmachina.api.PluginRegistrator;
import commoble.exmachinaessentials.content.wire_post.WirePostBlock;
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
