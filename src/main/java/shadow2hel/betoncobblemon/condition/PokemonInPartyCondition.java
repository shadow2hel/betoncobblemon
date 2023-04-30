package shadow2hel.betoncobblemon.condition;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.CobblemonEntities;
import com.cobblemon.mod.common.api.storage.NoPokemonStoreException;
import com.cobblemon.mod.common.api.storage.PokemonStore;
import com.cobblemon.mod.common.api.storage.party.PlayerPartyStore;
import com.cobblemon.mod.common.pokemon.Pokemon;
import net.minecraft.server.level.ServerPlayer;
import org.betonquest.betonquest.Instruction;
import org.betonquest.betonquest.api.Condition;
import org.betonquest.betonquest.api.profiles.Profile;
import org.betonquest.betonquest.exceptions.InstructionParseException;
import org.betonquest.betonquest.exceptions.QuestRuntimeException;
import shadow2hel.betoncobblemon.util.PokeSelector;

import java.util.Set;
import java.util.stream.StreamSupport;

public class PokemonInPartyCondition extends Condition {
    private PokeSelector pokeSelector;
    private boolean exactMatch;
    public PokemonInPartyCondition(Instruction instruction) throws InstructionParseException {
        super(instruction, true);
        pokeSelector = new PokeSelector(instruction.next());
        exactMatch = instruction.hasArgument("exactMatch");
    }

    @Override
    protected Boolean execute(Profile profile) throws QuestRuntimeException {
        PlayerPartyStore pokeParty = null;
        try {
           pokeParty = Cobblemon.INSTANCE.getStorage().getParty(profile.getPlayerUUID());
        } catch (NoPokemonStoreException e) {
            throw new RuntimeException(e);
        }
        boolean isPokemonPresent;
        if(exactMatch)
            isPokemonPresent = StreamSupport.stream(pokeParty.spliterator(), false)
                    .allMatch(pokemon -> pokeSelector.matches(pokemon));
        else {
            isPokemonPresent = pokeSelector.containsAll(pokeParty);
        }
        return isPokemonPresent;
    }
}
