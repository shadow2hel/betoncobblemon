package shadow2hel.betoncobblemon.objective;

import com.cobblemon.mod.common.CobblemonItems;
import com.cobblemon.mod.common.api.Priority;
import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon;
import com.cobblemon.mod.common.pokemon.EVs;
import com.cobblemon.mod.common.pokemon.Pokemon;
import kotlin.Unit;
import org.betonquest.betonquest.Instruction;
import org.betonquest.betonquest.api.profiles.OnlineProfile;
import org.betonquest.betonquest.api.profiles.Profile;
import org.betonquest.betonquest.exceptions.InstructionParseException;
import org.betonquest.betonquest.utils.PlayerConverter;
import org.bukkit.Bukkit;
import shadow2hel.betoncobblemon.util.PokeSelector;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class TrainEVObjective extends PokeObjective {

    private final Integer totalEVs;

    public TrainEVObjective(Instruction instruction) throws InstructionParseException {
        super(instruction, new PokeSelector(instruction.next()));
        template = TrainEVData.class;
        this.totalEVs = instruction.getInt();
        if (totalEVs > EVs.MAX_TOTAL_VALUE)
            throw new InstructionParseException("Parameter TotalEVs: " + totalEVs + " is too high!");
        targetAmount = instruction.getVarNum();
    }

    @Override
    public void start() {
        eventHandler = CobblemonEvents.BATTLE_VICTORY.subscribe(Priority.NORMAL, event -> {
            event.getWinners().forEach(winner -> winner.getPokemonList().stream()
                    .filter(winnerPoke -> winnerPoke.getHealth() > 0
                    && (winnerPoke.getFacedOpponents().size() > 0
                    || winnerPoke.getEffectedPokemon().heldItem().equals(CobblemonItems.EXP_SHARE.get().getDefaultInstance())))
                    .map(BattlePokemon::getEffectedPokemon)
                    .filter(pokeSelector::matches)
                    .filter(matchedPoke ->
                            StreamSupport.stream(matchedPoke.getEvs().spliterator(), false)
                            .reduce(0, (subtotalEVs, stat) -> subtotalEVs + stat.getValue().intValue(), Integer::sum)
                            >= totalEVs)
                    .forEach(pokeWithEVs -> {
                        UUID player = pokeWithEVs.getOwnerUUID();
                        if (player == null) return;
                        OnlineProfile onlineProfile = PlayerConverter.getID(Bukkit.getPlayer(player));
                        TrainEVData evData = (TrainEVData) getCountingData(onlineProfile);
                        if (containsPlayer(onlineProfile) && checkConditions(onlineProfile)
                                && evData.tryProgress(pokeWithEVs))
                            handleDataChange(onlineProfile, evData);
                    }));
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
        final String message = "pokemon_to_train";
        completeIfDoneOrNotify(onlineProfile, message);
    }

    public static class TrainEVData extends CountingData {
        private final Set<UUID> pokemonAlreadyUsed;

        public TrainEVData(String instruction, Profile profile, String objID) {
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
