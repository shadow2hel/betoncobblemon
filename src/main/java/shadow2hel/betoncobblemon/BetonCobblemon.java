package shadow2hel.betoncobblemon;

import com.cobblemon.mod.common.Cobblemon;
import net.minecraftforge.fml.ModList;
import org.betonquest.betonquest.BetonQuest;
import org.bukkit.plugin.java.JavaPlugin;
import shadow2hel.betoncobblemon.condition.PokemonInPartyCondition;
import shadow2hel.betoncobblemon.objective.BattleVictoryObjective;
import shadow2hel.betoncobblemon.objective.CatchObjective;
import shadow2hel.betoncobblemon.objective.EvolutionObjective;
import shadow2hel.betoncobblemon.objective.FriendshipObjective;

public final class BetonCobblemon extends JavaPlugin {

    @Override
    public void onEnable() {
        if (!ModList.get().isLoaded(Cobblemon.MODID)) {
            getLogger().warning("Cobblemon must be installed!");
            this.onDisable();
        }
        BetonQuest.getInstance().registerObjectives("pokecatch", CatchObjective.class);
        BetonQuest.getInstance().registerObjectives("pokebattle", BattleVictoryObjective.class);
        BetonQuest.getInstance().registerObjectives("pokeevolve", EvolutionObjective.class);
        BetonQuest.getInstance().registerObjectives("pokefriendship", FriendshipObjective.class);

        BetonQuest.getInstance().registerConditions("pokeinparty", PokemonInPartyCondition.class);
    }

    @Override
    public void onDisable() {
        getLogger().info("Shutting down..");
    }
}
