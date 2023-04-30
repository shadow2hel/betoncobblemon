package shadow2hel.betoncobblemon.objective;

import com.cobblemon.mod.common.api.Priority;
import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.cobblemon.mod.common.pokemon.Pokemon;
import kotlin.Unit;
import org.betonquest.betonquest.Instruction;
import org.betonquest.betonquest.api.profiles.OnlineProfile;
import org.betonquest.betonquest.api.profiles.Profile;
import org.betonquest.betonquest.exceptions.InstructionParseException;
import org.betonquest.betonquest.utils.PlayerConverter;
import org.bukkit.Bukkit;
import shadow2hel.betoncobblemon.util.PokeSelector;

import java.util.*;
import java.util.stream.Collectors;

public class FriendshipObjective extends PokeObjective {
    private final Integer friendshipLevel;
    public FriendshipObjective(Instruction instruction) throws InstructionParseException {
        super(instruction, new PokeSelector(instruction.next()));
        template = FriendshipData.class;
        friendshipLevel = instruction.getInt();
        targetAmount = instruction.getVarNum();
    }

    @Override
    public void start() {
        eventHandler = CobblemonEvents.FRIENDSHIP_UPDATED.subscribe(Priority.NORMAL, event -> {
            Optional<UUID> playerUUID = Optional.ofNullable(event.getPokemon().getOwnerUUID());
            Pokemon pokemon = event.getPokemon();
            if (playerUUID.isEmpty())
                return Unit.INSTANCE;
            OnlineProfile onlineProfile = PlayerConverter.getID(Bukkit.getPlayer(playerUUID.get()));
            if (containsPlayer(onlineProfile) && pokeSelector.matches(pokemon)
                    && event.getNewFriendship() == friendshipLevel
                    && checkConditions(onlineProfile)) {

                FriendshipData friendData = (FriendshipData) getCountingData(onlineProfile);
                if(friendData.tryProgress(pokemon))
                    handleDataChange(onlineProfile, friendData);
            }
            return Unit.INSTANCE;
        });
    }

    @Override
    public void stop() {
        if (eventHandler != null)
            eventHandler.unsubscribe();
    }

    @Override
    protected void handleDataChange(OnlineProfile onlineProfile, CountingData data) {
        final String message = "pokemon_to_befriend";
        completeIfDoneOrNotify(onlineProfile, message);
    }

    public static class FriendshipData extends CountingData {
        private final Set<UUID> pokemonAlreadyUsed;
        public FriendshipData(String instruction, Profile profile, String objID) {
            super(instruction, profile, objID);
            pokemonAlreadyUsed = new HashSet<>();
            final String[] pokesInstruction = instruction.split(";", 3);
            if (pokesInstruction.length >= 2 && !pokesInstruction[1].isEmpty()) {
                Arrays.stream(pokesInstruction[1].split("/"))
                        .map(UUID::fromString)
                        .forEach(pokemonAlreadyUsed::add);
            }
        }

        public boolean tryProgress(final Pokemon pokemon) {
            final boolean added = pokemonAlreadyUsed.add(pokemon.getUuid());
            if (added)
                progress();
            return added;
        }

        @Override
        public String toString() {
            return super.toString() + ";" + pokemonAlreadyUsed.stream().map(UUID::toString).collect(Collectors.joining("/"));
        }
    }
}
