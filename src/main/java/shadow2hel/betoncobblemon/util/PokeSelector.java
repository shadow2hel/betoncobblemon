package shadow2hel.betoncobblemon.util;

import com.cobblemon.mod.common.api.pokeball.PokeBalls;
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies;
import com.cobblemon.mod.common.api.pokemon.egg.EggGroup;
import com.cobblemon.mod.common.pokeball.PokeBall;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.pokemon.Species;
import org.betonquest.betonquest.api.BetonQuestLogger;
import org.betonquest.betonquest.exceptions.InstructionParseException;

import java.util.*;
import java.util.stream.Collectors;

public class PokeSelector {
    private static final BetonQuestLogger LOG = BetonQuestLogger.create();
    private final Map<Species, PokeSpecifics> species;

    public PokeSelector(final String pokeInputWithParameters) throws InstructionParseException {
        if (pokeInputWithParameters.isEmpty())
            throw new InstructionParseException("Invalid PokeSelector, no input was given!");
        this.species = processPokes(pokeInputWithParameters);
    }

    private boolean isEggGroup(String eggGroup) {
        EggGroup.valueOf(eggGroup.toUpperCase());
        return true;
    }

    private Map<Species, PokeSpecifics> processPokes(String pokesWithParams) throws InstructionParseException {
        Map<Species, PokeSpecifics> pokesToAdd = new HashMap<>();
        String[] pokesToProcess = pokesWithParams.split(";");

        for (String pokeRaw : pokesToProcess) {
            final int bracketOpenIndex = pokeRaw.indexOf('[');
            final int bracketClosedIndex = pokeRaw.indexOf(']');

            final String pokeFound = pokeRaw.substring(0, bracketOpenIndex);
            final String pokeParamsRaw = pokeRaw.substring(bracketOpenIndex + 1, bracketClosedIndex);

            Set<Species> speciesFound = getSpeciesOrGroup(pokeFound);
            PokeSpecifics specifics = new PokeSpecifics().parseString(pokeParamsRaw);

            for (Species speciesToAdd : speciesFound) {
                pokesToAdd.put(speciesToAdd, specifics);
            }
        }

        return pokesToAdd;
    }

    private Set<Species> getSpeciesOrGroup(String speciesOrEggGroup) throws InstructionParseException{
        Set<Species> pokesToAdd = new HashSet<>();
        if (isEggGroup(speciesOrEggGroup)) {
            final EggGroup finalEgg = EggGroup.valueOf(speciesOrEggGroup.toUpperCase());
            Set<Species> insertingPokes = PokemonSpecies.INSTANCE
                    .getSpecies()
                    .stream()
                    .filter(species -> species.getEggGroups().contains(finalEgg))
                    .collect(Collectors.toSet());
            pokesToAdd.addAll(insertingPokes);
        } else {
            Species pokeToAdd = PokemonSpecies.INSTANCE.getByName(speciesOrEggGroup);
            if (pokeToAdd == null)
                throw new InstructionParseException(speciesOrEggGroup + " is not a valid Pokemon and/or Egg Group!");
            pokesToAdd.add(pokeToAdd);
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
        return species.entrySet()
                .stream()
                .anyMatch(speciesEntry -> speciesEntry.getKey().equals(pokemonToCheck.getSpecies())
                        && speciesEntry.getValue().matches(pokemonToCheck));
    }

    private class PokeSpecifics {
        private Integer minLevelRequired;
        private Integer maxLevelRequired;
        private Set<PokeBall> pokeBalls;
        private boolean shinyRequired;

        private PokeSpecifics() {
        }


        private PokeSpecifics parseString(String pokeParametersRaw) throws InstructionParseException {
            String[] paramsWithValues = pokeParametersRaw.split(",");
            for (String paramWithValue : paramsWithValues) {
                String[] paramSplitValue = paramWithValue.split(":");
                // Only standalone parameter we have is shiny, so we check for that here
                if (paramSplitValue.length == 1) {
                    this.requireShiny(paramSplitValue[0].equalsIgnoreCase("shiny"));
                } else {
                    final String parameter = paramSplitValue[0];
                    final String value = paramSplitValue[1];
                    switch (parameter.toLowerCase()) {
                        case "minlevel" -> {
                            final int minlevel = Integer.parseInt(value);
                            if (minlevel < 0)
                                throw new InstructionParseException("Mininum level cannot be below 0!");
                            requireMinLevel(minlevel);
                        }
                        case "maxlevel" -> {
                            final int maxlevel = Integer.parseInt(value);
                            if (maxlevel < 0)
                                throw new InstructionParseException("Maximum level cannot be below 0!");
                            requireMaxLevel(maxlevel);
                        }
                        case "pokeballs" -> {
                            String[] allBalls = value.split(";");
                            Set<PokeBall> balls = new HashSet<>();
                            for (String strBall : allBalls) {
                                PokeBalls.INSTANCE.all()
                                        .stream()
                                        .filter(ball -> ball.getName().getPath().equalsIgnoreCase(strBall))
                                        .findAny()
                                        .ifPresent(balls::add);
                            }
                            if (balls.size() > 0) {
                                requireSpecificPokeballs(balls);
                            } else
                                throw new InstructionParseException("The given pokeballs weren't valid!");
                        }
                        default -> {
                            throw new InstructionParseException("Parameter: " + parameter + " is not a valid specifier!");
                        }
                    }
                }

            }
            return this;
        }

        private PokeSpecifics requireMinLevel(Integer minLvl) {
            this.minLevelRequired = minLvl;
            return this;
        }

        private PokeSpecifics requireMaxLevel(Integer maxLvl) {
            this.maxLevelRequired = maxLvl;
            return this;
        }

        private PokeSpecifics requireSpecificPokeballs(Set<PokeBall> pokeBalls) {
            this.pokeBalls = pokeBalls;
            return this;
        }

        private PokeSpecifics requireShiny(boolean shinyRequired) {
            this.shinyRequired = shinyRequired;
            return this;
        }

        private boolean matches(Pokemon pokemonToCheck) {
            if (shinyRequired && !pokemonToCheck.getShiny()) {
                return false;
            }
            if (pokeBalls.size() > 0 && !pokeBalls.contains(pokemonToCheck.getCaughtBall()))
                return false;
            return (minLevelRequired <= 0 || minLevelRequired <= pokemonToCheck.getLevel())
                    && (maxLevelRequired <= 0 || pokemonToCheck.getLevel() <= maxLevelRequired);
        }
    }
}
