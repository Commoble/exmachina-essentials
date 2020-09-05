package commoble.exmachinaessentials;

import commoble.exmachinaessentials.util.ConfigHelper;
import commoble.exmachinaessentials.util.ConfigHelper.ConfigValueListener;
import net.minecraftforge.common.ForgeConfigSpec;

public class ServerConfig
{
	public final ConfigValueListener<Double> max_wire_post_connection_range;
	public final ConfigValueListener<Integer> solar_panel_update_interval;
	public final ConfigValueListener<Double> max_battery_box_energy;
	
	public ServerConfig(ForgeConfigSpec.Builder builder, ConfigHelper.Subscriber subscriber)
	{
		builder.push("Block Settings");
		this.max_wire_post_connection_range = subscriber.subscribe(builder
			.comment("Maximum Wire Post Connection Range")
			.translation("exmachinaessentials.config.max_wire_plinth_connection_range")
			.defineInRange("max_wire_post_connection_range", 32D, 0D, Double.MAX_VALUE));
		this.solar_panel_update_interval = subscriber.subscribe(builder
			.comment("Interval between solar panel updates (in ticks)")
			.translation("exmachinaessentials.config.solar_panel_udpate_interval")
			.defineInRange("solar_panel_update_interval", 200, 1, Integer.MAX_VALUE));
		this.max_battery_box_energy = subscriber.subscribe(builder
			.comment("Maximum energy storable by battery boxes (in Joules or Watt-hours")
			.translation("exmachinaessentials.max_battery_box_energy")
			.defineInRange("max_battery_box_energy",  1000D, 1, Double.MAX_VALUE));
		builder.pop();
	}
}
