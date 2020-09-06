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
	
		registry.registerDynamicCircuitElementProperty(new ResourceLocation("exmachinaessentials:rise_until_battery_charged"), EssentialsPlugin::readRiseUntilBatteryCharged);
		registry.registerDynamicCircuitElementProperty(new ResourceLocation("exmachinaessentials:inverting_battery_charger"), EssentialsPlugin::readInvertingBatteryCharger);
	}
	
	/**
	 * Read a json in the following format (numbers are examples):
	 * "dynamic_source":
	 * {
	 * 	"type": "exmachinaessentials:rise_until_battery_charged",
	 * 	"max": 10.0,
	 * 	"falloff": 0.1
	 * }
	 * @param json A json object in the above format, where
	 * "max" is a field containing the value of the property when the battery's stored energy is below the falloff value.
	 * The max value will default to 0 if not specified.
	 * "falloff" is the *percentage* of energy stored in the battery below which the output value will begin to decrease.
	 * The falloff value must be greater than 0 to avoid division errors. If falloff is not positive or not specified, it defaults to 1.0 (100%)
	 * @return A dynamic property whose output scales quadratically from 0 to the max value when the battery's stored energy is between
	 * 0 and the falloff value, and whose output is the max value when the stored energy is above that
	 */
	private static DynamicPropertyFactory readRiseUntilBatteryCharged(JsonObject json)
	{
		double max = getJsonDouble(json, "max", 0D);
		double falloff = getJsonDouble(json, "falloff", 1D);
		
		return block -> block instanceof BatteryBoxBlock
			? (world,pos,state) -> ((BatteryBoxBlock)block).getRisingAttenuation(world,pos,state,max,falloff)
			: (world,pos,state) -> 0D;
	}
	
	/**
	 * Read a json in the following format (numbers are examples):
	 * "dynamic_load":
	 * {
	 * 	"type": "exmachinaessentials:inverting_battery_charger",
	 * "charging": 20.0,
	 * "discharging": 0.1
	 * }
	 * @param json A json object in the above format, where
	 * "charging" is a field containing the output value while the battery is gaining energy (default 0)
	 * "discharging" is a field containing the output value while the battery is losing energy (default 0)
	 * @return A dynamic property whose output depends on whether the battery is currently gaining or losing energy
	 */
	private static DynamicPropertyFactory readInvertingBatteryCharger(JsonObject json)
	{
		double charging = getJsonDouble(json, "charging", 0D);
		double discharging = getJsonDouble(json, "discharging", 0D);
		double breakpoint = getJsonDouble(json, "breakpoint", 0D);
		
		return block -> block instanceof BatteryBoxBlock
			? (world,pos,state) -> ((BatteryBoxBlock)block).getInvertableChargerValue(world,pos,state,charging,discharging,breakpoint)
			: (world,pos,state) -> charging; // reasoning: return what the value would be given an uncharged battery
	}
	
	private static double getJsonDouble(JsonObject json, String name, double defaultIfNotPresent)
	{
		JsonElement element = json.get(name);
		return element == null ? defaultIfNotPresent : element.getAsDouble();
	}
}
