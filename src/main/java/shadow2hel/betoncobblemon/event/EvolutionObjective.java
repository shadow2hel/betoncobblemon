package shadow2hel.betoncobblemon.event;

import com.cobblemon.mod.common.api.Priority;
import com.cobblemon.mod.common.api.events.CobblemonEvents;
import kotlin.Unit;
import org.betonquest.betonquest.Instruction;
import org.betonquest.betonquest.api.profiles.OnlineProfile;
import org.betonquest.betonquest.exceptions.InstructionParseException;
import org.betonquest.betonquest.utils.PlayerConverter;
import org.bukkit.Bukkit;
import shadow2hel.betoncobblemon.util.PokeSelector;

import java.util.Optional;
import java.util.UUID;

public class EvolutionObjective extends PokeObjective {

    public EvolutionObjective(Instruction instruction) throws InstructionParseException {
        super(instruction, new PokeSelector(instruction.next()));
        targetAmount = instruction.getVarNum();
    }

    @Override
    public void start() {
        eventHandler = CobblemonEvents.EVOLUTION_COMPLETE.subscribe(Priority.NORMAL, event -> {
            Optional<UUID> playerUUID = Optional.ofNullable(event.getPokemon().getOwnerUUID());
            if(playerUUID.isEmpty())
                return Unit.INSTANCE;
            OnlineProfile onlineProfile = PlayerConverter.getID(Bukkit.getPlayer(playerUUID.get()));
            if (containsPlayer(onlineProfile) && pokeSelector.matches(event.getPokemon()) && checkConditions(onlineProfile)) {
                handleDataChange(onlineProfile, getCountingData(onlineProfile).add());
            }
            return Unit.INSTANCE;
        });
    }

    @Override
    public void stop() {
        if(eventHandler != null)
            eventHandler.unsubscribe();
    }

    @Override
    protected void handleDataChange(OnlineProfile onlineProfile, CountingData data) {
        final String message = "pokemon_to_evolve";
        completeIfDoneOrNotify(onlineProfile, message);
    }
}
