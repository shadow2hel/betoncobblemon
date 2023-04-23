package shadow2hel.betoncobblemon.event;

import com.cobblemon.mod.common.api.events.pokemon.PokemonCapturedEvent;
import com.cobblemon.mod.common.api.reactive.ObservableSubscription;
import org.betonquest.betonquest.Instruction;
import org.betonquest.betonquest.api.CountingObjective;
import org.betonquest.betonquest.exceptions.InstructionParseException;
import shadow2hel.betoncobblemon.util.PokeSelector;

public class BattleVictoryObjective extends CountingObjective {
    private final PokeSelector pokeSelector;
    private ObservableSubscription<PokemonCapturedEvent> eventHandler;

    public BattleVictoryObjective(final Instruction instruction) throws InstructionParseException {
        super(instruction);
        pokeSelector = new PokeSelector(instruction.next());
        targetAmount = instruction.getVarNum();
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }
}
