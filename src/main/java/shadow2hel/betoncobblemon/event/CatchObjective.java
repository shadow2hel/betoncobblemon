package shadow2hel.betoncobblemon.event;

import com.cobblemon.mod.common.api.Priority;
import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.cobblemon.mod.common.api.events.pokemon.PokemonCapturedEvent;
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies;
import com.cobblemon.mod.common.api.reactive.ObservableSubscription;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.pokemon.activestate.PokemonState;
import kotlin.Unit;
import org.betonquest.betonquest.Instruction;
import org.betonquest.betonquest.VariableNumber;
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
        pokeSelector = new PokeSelector(instruction.next(),
                Integer.parseInt(instruction.next()),
                Integer.parseInt(instruction.next()),
                instruction.next(),
                instruction.hasArgument("shiny"));
        targetAmount = instruction.getVarNum();
    }

    @Override
    public String getDefaultDataInstruction(Profile profile) {
        return String.valueOf(targetAmount.getInt(profile));
    }

    @Override
    public void start() {
        eventHandler = CobblemonEvents.POKEMON_CAPTURED.subscribe(Priority.NORMAL, event -> {
            final OnlineProfile onlineProfile = PlayerConverter.getID(Bukkit.getPlayer(event.getPlayer().getUUID()));
            if (containsPlayer(onlineProfile) && pokeSelector.matches(event.getPokemon()) && checkConditions(onlineProfile)) {
                handleDataChange(onlineProfile, getCountingData(onlineProfile).add());
            }
            return Unit.INSTANCE;
        });
    }

    @Override
    public void stop() {
        eventHandler.unsubscribe();
    }

    private void handleDataChange(final OnlineProfile onlineProfile, final CountingData data) {
        final String message = "pokemon_to_catch";
        completeIfDoneOrNotify(onlineProfile, message);
    }

}
