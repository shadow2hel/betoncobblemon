package shadow2hel.betoncobblemon.objective;

import com.cobblemon.mod.common.api.Priority;
import com.cobblemon.mod.common.api.battles.model.actor.ActorType;
import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.cobblemon.mod.common.battles.BattleSide;
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon;
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
import java.util.stream.StreamSupport;

public class BattleVictoryObjective extends PokeObjective {
    private ActorType typeOfEnemy;

    public BattleVictoryObjective(final Instruction instruction) throws InstructionParseException {
        super(instruction, new PokeSelector(instruction.next()));
        template = BattleVictoryData.class;
        targetAmount = instruction.getVarNum();
        Optional<String> enemyType = instruction.getOptionalArgument("type");
        typeOfEnemy = null;
        if (enemyType.isPresent()) {
            try {
                typeOfEnemy = ActorType.valueOf(enemyType.get().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new InstructionParseException("Parameter: " + enemyType.get() + " is not a valid enemy type!");
            }
        }
    }

    @Override
    public void start() {
        eventHandler = CobblemonEvents.BATTLE_VICTORY.subscribe(Priority.NORMAL, event -> {
            if (typeOfEnemy != null && event.getBattle().isPvN() && !typeOfEnemy.equals(ActorType.NPC))
                return Unit.INSTANCE;
            if (typeOfEnemy != null && event.getBattle().isPvW() && !typeOfEnemy.equals(ActorType.WILD))
                return Unit.INSTANCE;
            if (typeOfEnemy != null && event.getBattle().isPvP() && !typeOfEnemy.equals(ActorType.PLAYER))
                return Unit.INSTANCE;
            event.getWinners()
                    .forEach(battleActor -> {
                        BattleSide loserSide = battleActor.getSide().getOppositeSide();

                        StreamSupport.stream(battleActor.getPlayerUUIDs().spliterator(), false)
                                .map(Bukkit::getPlayer)
                                .map(PlayerConverter::getID)
                                .forEach(onlineProfile -> {
                                    Arrays.stream(loserSide.getActors())
                                            .forEach(loserActor -> {
                                                BattleVictoryData battleData = (BattleVictoryData) getCountingData(onlineProfile);
                                                Set<UUID> alreadyDefeated = ((BattleVictoryData) getCountingData(onlineProfile)).getDefeatedPlayers();
                                                UUID newPlayer = StreamSupport.stream(loserActor.getPlayerUUIDs().spliterator(), false)
                                                        .filter(loserUUID -> loserActor.getType() == ActorType.PLAYER
                                                                && !alreadyDefeated.contains(loserUUID))
                                                        .findFirst()
                                                        .orElse(null);

                                                if (event.getBattle().isPvP() && newPlayer != null) {
                                                    boolean isUnknownPlayer = battleData.tryProgress(newPlayer);
                                                    if (isUnknownPlayer) {
                                                        loserActor.getPokemonList().stream()
                                                                .map(BattlePokemon::getOriginalPokemon)
                                                                .forEach(loserPokemon -> {
                                                                    if (containsPlayer(onlineProfile) && pokeSelector.matches(loserPokemon) && checkConditions(onlineProfile)) {
                                                                        handleDataChange(onlineProfile, battleData.add());
                                                                    }
                                                                });
                                                    }
                                                } else if(!event.getBattle().isPvP()) {
                                                    loserActor.getPokemonList().stream()
                                                            .map(BattlePokemon::getOriginalPokemon)
                                                            .forEach(loserPokemon -> {
                                                                if (containsPlayer(onlineProfile) && pokeSelector.matches(loserPokemon) && checkConditions(onlineProfile)) {
                                                                    handleDataChange(onlineProfile, battleData.add());
                                                                }
                                                            });
                                                }
                                            });
                                });
                    });
            return Unit.INSTANCE;
        });
    }

    @Override
    public void stop() {
        if (eventHandler != null)
            eventHandler.unsubscribe();
    }

    protected void handleDataChange(final OnlineProfile onlineProfile, final CountingData data) {
        final String message = "pokemon_to_defeat";
        completeIfDoneOrNotify(onlineProfile, message);
    }

    public static class BattleVictoryData extends CountingData {
        private final Set<UUID> defeatedPlayers;
        public BattleVictoryData(String instruction, Profile profile, String objID) {
            super(instruction, profile, objID);
            defeatedPlayers = new HashSet<>();
            final String[] pokesInstruction = instruction.split(";", 3);
            if (pokesInstruction.length >= 2 && !pokesInstruction[1].isEmpty()) {
                Arrays.stream(pokesInstruction[1].split("/"))
                        .map(UUID::fromString)
                        .forEach(defeatedPlayers::add);
            }
        }

        public boolean tryProgress(final UUID player) {
            final boolean added = defeatedPlayers.add(player);
            if (added)
                progress();
            return added;
        }

        public Set<UUID> getDefeatedPlayers() {
            return defeatedPlayers;
        }

        @Override
        public String toString() {
            return super.toString() + ";" + defeatedPlayers.stream().map(UUID::toString).collect(Collectors.joining("/"));
        }
    }
}
