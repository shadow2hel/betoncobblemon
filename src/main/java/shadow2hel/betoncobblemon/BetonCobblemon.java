package shadow2hel.betoncobblemon;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.events.CobblemonEvents;
import org.bukkit.plugin.java.JavaPlugin;
import org.checkerframework.checker.units.qual.C;
import shadow2hel.betoncobblemon.event.CatchEvent;

public final class BetonCobblemon extends JavaPlugin {

    @Override
    public void onEnable() {
        // Plugin startup logic
        CatchEvent event = new CatchEvent(this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
