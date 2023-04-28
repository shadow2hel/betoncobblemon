package shadow2hel.betoncobblemon.event;

import com.cobblemon.mod.common.api.reactive.ObservableSubscription;
import org.betonquest.betonquest.Instruction;
import org.betonquest.betonquest.api.CountingObjective;
import org.betonquest.betonquest.api.profiles.OnlineProfile;
import org.betonquest.betonquest.api.profiles.Profile;
import org.betonquest.betonquest.exceptions.InstructionParseException;
import shadow2hel.betoncobblemon.util.PokeSelector;

public abstract class PokeObjective extends CountingObjective {
    protected PokeSelector pokeSelector;
    protected ObservableSubscription<?> eventHandler;

    public PokeObjective(Instruction instruction, PokeSelector pokeSelector) throws InstructionParseException {
        super(instruction);
        this.pokeSelector = pokeSelector;
    }

    @Override
    public String getDefaultDataInstruction(Profile profile) {
        return String.valueOf(targetAmount.getInt(profile));
    }

    protected abstract void handleDataChange(final OnlineProfile onlineProfile, final CountingData data);
}
