package shadow2hel.betoncobblemon.event;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.Priority;
import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.cobblemon.mod.forge.CobblemonForge;
import io.izzel.arclight.api.Arclight;
import kotlin.Unit;
import org.betonquest.betonquest.api.QuestEvent;
import org.betonquest.betonquest.api.profiles.Profile;
import org.betonquest.betonquest.exceptions.QuestRuntimeException;
import org.bukkit.plugin.java.JavaPlugin;

public class CatchEvent extends QuestEvent {
    public CatchEvent(JavaPlugin main) {
        CobblemonEvents.POKEMON_CAPTURED.subscribe(Priority.NORMAL, event -> {
            main.getLogger().info(event.getPokemon().getSpecies().getName() + " Caught!");
            return Unit.INSTANCE;
        });
    }

    @Override
    protected Void execute(Profile profile) throws QuestRuntimeException {
        return null;
    }
}
