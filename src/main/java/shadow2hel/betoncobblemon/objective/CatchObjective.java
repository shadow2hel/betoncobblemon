package shadow2hel.betoncobblemon.objective;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.Priority;
import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.cobblemon.mod.common.api.pokemon.egg.EggGroup;
import com.cobblemon.mod.common.pokemon.Species;
import kotlin.Unit;
import org.betonquest.betonquest.Instruction;
import org.betonquest.betonquest.api.profiles.OnlineProfile;
import org.betonquest.betonquest.exceptions.InstructionParseException;
import org.betonquest.betonquest.utils.PlayerConverter;
import org.bukkit.Bukkit;
import shadow2hel.betoncobblemon.util.PokeSelector;

public class CatchObjective extends PokeObjective {

    public CatchObjective(final Instruction instruction) throws InstructionParseException {
        super(instruction, new PokeSelector(instruction.next()));
        targetAmount = instruction.getVarNum();
    }

    @Override
    public void start() {
        eventHandler = CobblemonEvents.POKEMON_CAPTURED.subscribe(Priority.NORMAL, event -> {
            final OnlineProfile onlineProfile = PlayerConverter.getID(Bukkit.getPlayer(event.getPlayer().getGameProfile().getId()));
            if (containsPlayer(onlineProfile) && pokeSelector.matches(event.getPokemon()) && checkConditions(onlineProfile)) {
                handleDataChange(onlineProfile, getCountingData(onlineProfile).add());
            }
            return Unit.INSTANCE;
        });
    }

    @Override
    public void stop() {
        if (eventHandler != null)
            eventHandler.unsubscribe();
    }

    protected void handleDataChange(final OnlineProfile onlineProfile, final CountingData data) {
        final String message = "pokemon_to_catch";
        completeIfDoneOrNotify(onlineProfile, message);
    }

}
