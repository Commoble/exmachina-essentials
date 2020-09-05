package commoble.exmachinaessentials.content;

import java.util.Map;
import java.util.function.Supplier;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import commoble.exmachina.api.AutoPlugin;
import commoble.exmachina.api.CircuitComponent;
import commoble.exmachina.api.DynamicPropertyFactory;
import commoble.exmachina.api.Plugin;
import commoble.exmachina.api.PluginRegistrator;
import commoble.exmachinaessentials.content.battery.BatteryBoxBlock;
import commoble.exmachinaessentials.content.wire_post.WirePostBlock;
import net.minecraft.block.Block;
import net.minecraft.util.ResourceLocation;

@AutoPlugin
public class EssentialsPlugin implements Plugin
{
	public static Supplier<Map<Block, ? extends CircuitComponent>> componentData;
	@Override
	public void register(PluginRegistrator registry)
	{
		componentData = registry.getComponentDataGetter();
		
		registry.registerConnectionType(new ResourceLocation("exmachinaessentials:wire_post"), json -> block -> WirePostBlock::getPotentialConnections);
	
		registry.registerDynamicCircuitElementProperty(new ResourceLocation("exmachinaessentials:attenuate_battery"), EssentialsPlugin::readAttenuateBattery);
	}
	
	/**
	 * Read a json in the following format (numbers are examples):
	 * "dynamic_source":
	 * {
	 * 	"type": "exmachinaessentials:attenuate_battery",
	 * 	"max": 10.0,
	 * 	"falloff": 0.1
	 * }
	 * @param json A json object in the above format, where
	 * "max" is a field containing the value of the property when the battery's stored energy is below the falloff value.
	 * The max value will default to 0 if not specified.
	 * "falloff" is the *percentage* of energy stored in the battery below which the output value will begin to decrease.
	 * The falloff value must be greater than 0 to avoid division errors. If falloff is not positive or not specified, it defaults to 1.0 (100%)
	 * @return A dynamic property whose output scales from 0 to the json's max value when the battery's stored energy is between
	 * 0 and the json's falloff value, and the json's max value when the stored energy is above that
	 */
	private static DynamicPropertyFactory readAttenuateBattery(JsonObject json)
	{
		JsonElement maxElement = json.get("max");
		double max = maxElement == null ? 0D : maxElement.getAsDouble();
		
		JsonElement falloffElement = json.get("falloff");
		double rawFalloff = falloffElement == null ? 1D : falloffElement.getAsDouble();
		double falloff = rawFalloff > 0 ? rawFalloff : 1D;
		
		return block -> block instanceof BatteryBoxBlock
			? (world,pos,state) -> ((BatteryBoxBlock)block).getAttenuation(world,pos,state,max,falloff)
			: (world,pos,state) -> 0D;
	}
}
