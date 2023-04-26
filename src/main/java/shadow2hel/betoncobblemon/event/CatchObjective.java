package shadow2hel.betoncobblemon.event;

import com.cobblemon.mod.common.api.Priority;
import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.cobblemon.mod.common.api.events.pokemon.PokemonCapturedEvent;
import com.cobblemon.mod.common.api.reactive.ObservableSubscription;
import kotlin.Unit;
import org.betonquest.betonquest.Instruction;
import org.betonquest.betonquest.api.CountingObjective;
import org.betonquest.betonquest.api.profiles.OnlineProfile;
import org.betonquest.betonquest.api.profiles.Profile;
import org.betonquest.betonquest.exceptions.InstructionParseException;
import org.betonquest.betonquest.utils.PlayerConverter;
import org.bukkit.Bukkit;
import shadow2hel.betoncobblemon.util.PokeSelector;

public class CatchObjective extends CountingObjective {
    private final PokeSelector pokeSelector;
    private ObservableSubscription<PokemonCapturedEvent> eventHandler;

    public CatchObjective(final Instruction instruction) throws InstructionParseException {
        super(instruction);
        pokeSelector = new PokeSelector(instruction.next());
        targetAmount = instruction.getVarNum();
    }

    @Override
    public String getDefaultDataInstruction(Profile profile) {
        return String.valueOf(targetAmount.getInt(profile));
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

    private void handleDataChange(final OnlineProfile onlineProfile, final CountingData data) {
        final String message = "pokemon_to_catch";
        completeIfDoneOrNotify(onlineProfile, message);
    }

}
