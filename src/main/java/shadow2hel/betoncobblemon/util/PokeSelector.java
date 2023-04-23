package shadow2hel.betoncobblemon.util;

import com.cobblemon.mod.common.api.pokeball.PokeBalls;
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies;
import com.cobblemon.mod.common.api.pokemon.egg.EggGroup;
import com.cobblemon.mod.common.pokeball.PokeBall;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.pokemon.Species;
import org.betonquest.betonquest.VariableNumber;
import org.betonquest.betonquest.api.BetonQuestLogger;
import org.betonquest.betonquest.exceptions.InstructionParseException;

import java.util.*;
import java.util.stream.Collectors;

public class PokeSelector {
    private static final BetonQuestLogger LOG = BetonQuestLogger.create();
    private final Set<Species> species;
    private final Integer minLevelRequired;
    private final Integer maxLevelRequired;
    private final Set<PokeBall> specificPokeballsRequired;
    private final boolean shinyRequired;

    public PokeSelector(final String pokesAndEggGroup,
                        final Integer minLevelRequired,
                        final Integer maxLevelRequired,
                        final String pokeballs,
                        final boolean shinyRequired) throws InstructionParseException {
        if (pokesAndEggGroup.isEmpty())
            throw new InstructionParseException("Invalid PokeSelector, no input was given!");
        this.species = processPokes(pokesAndEggGroup);
        this.minLevelRequired = minLevelRequired;
        this.maxLevelRequired = maxLevelRequired;
        this.specificPokeballsRequired = processPokeballs(pokeballs);
        this.shinyRequired = shinyRequired;
    }

    private boolean isEggGroup(String eggGroup) {
        EggGroup.valueOf(eggGroup.toUpperCase());
        return true;
    }

    private Set<Species> processPokes(String input) throws InstructionParseException {
        String[] potentialPoke = input.split(",");
        Set<Species> pokesToAdd = new HashSet<>();
        for (String arg : potentialPoke) {
            if (isEggGroup(arg)) {
                final EggGroup finalEgg = EggGroup.valueOf(arg.toUpperCase());
                Set<Species> insertingPokes = PokemonSpecies.INSTANCE
                        .getSpecies()
                        .stream()
                        .filter(species -> species.getEggGroups().contains(finalEgg))
                        .collect(Collectors.toSet());
                pokesToAdd.addAll(insertingPokes);
            } else {
                Species pokeToAdd = PokemonSpecies.INSTANCE.getByName(arg);
                if (pokeToAdd == null)
                    throw new InstructionParseException(arg + " is not a valid Pokemon!");
                pokesToAdd.add(pokeToAdd);
            }
        }
        return pokesToAdd;
    }

    private Set<PokeBall> processPokeballs(String input) {
        String[] potentialBalls = input.split(",");
        Set<PokeBall> ballsToAdd = new HashSet<>();
        for (String ballInput : potentialBalls) {
            PokeBalls.INSTANCE.all()
                    .stream()
                    .filter(ball -> ball.getName().getPath().equalsIgnoreCase(ballInput))
                    .findAny()
                    .ifPresent(ballsToAdd::add);
        }
        return ballsToAdd;
    }

    public boolean matches(Pokemon pokemonToCheck) {
        if (species.contains(pokemonToCheck.getSpecies())) {
            if (shinyRequired && !pokemonToCheck.getShiny()) {
                return false;
            }
            if (specificPokeballsRequired.size() > 0 && !specificPokeballsRequired.contains(pokemonToCheck.getCaughtBall()))
                return false;
            return (minLevelRequired <= 0 || minLevelRequired <= pokemonToCheck.getLevel())
                    && (maxLevelRequired <= 0 || pokemonToCheck.getLevel() <= maxLevelRequired);
        }
        return false;
    }
}
