package shadow2hel.betoncobblemon.event;

import com.cobblemon.mod.common.api.Priority;
import com.cobblemon.mod.common.api.battles.model.actor.ActorType;
import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.cobblemon.mod.common.battles.BattleSide;
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon;
import kotlin.Unit;
import org.betonquest.betonquest.Instruction;
import org.betonquest.betonquest.api.profiles.OnlineProfile;
import org.betonquest.betonquest.exceptions.InstructionParseException;
import org.betonquest.betonquest.utils.PlayerConverter;
import org.bukkit.Bukkit;
import shadow2hel.betoncobblemon.util.PokeSelector;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.StreamSupport;

public class BattleVictoryObjective extends PokeObjective {
    private ActorType typeOfEnemy;

    public BattleVictoryObjective(final Instruction instruction) throws InstructionParseException {
        super(instruction, new PokeSelector(instruction.next()));
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
                                .forEach(player -> {
                                    final OnlineProfile onlineProfile = PlayerConverter.getID(player);
                                    Arrays.stream(loserSide.getActors())
                                            .flatMap(pokes -> pokes.getPokemonList().stream())
                                            .map(BattlePokemon::getOriginalPokemon)
                                            .forEach(loserPokemon -> {
                                                if (containsPlayer(onlineProfile) && pokeSelector.matches(loserPokemon) && checkConditions(onlineProfile)) {
                                                    handleDataChange(onlineProfile, getCountingData(onlineProfile).add());
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
}
