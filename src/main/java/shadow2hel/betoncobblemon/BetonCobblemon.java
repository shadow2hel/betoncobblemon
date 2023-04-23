package shadow2hel.betoncobblemon;

import com.cobblemon.mod.common.Cobblemon;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoader;
import org.betonquest.betonquest.BetonQuest;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import shadow2hel.betoncobblemon.event.CatchObjective;

public final class BetonCobblemon extends JavaPlugin {

    @Override
    public void onEnable() {
        if (!ModList.get().isLoaded(Cobblemon.MODID)) {
            getLogger().warning("Cobblemon must be installed!");
            this.onDisable();
        }
        BetonQuest.getInstance().registerObjectives("pokecatch", CatchObjective.class);
    }

    @Override
    public void onDisable() {
        getLogger().info("Shutting down..");
    }
}
