package shadow2hel.betoncobblemon.util;

import com.cobblemon.mod.common.api.pokemon.PokemonSpecies;
import com.cobblemon.mod.common.api.pokemon.egg.EggGroup;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.pokemon.Species;
import org.betonquest.betonquest.exceptions.InstructionParseException;

import java.util.*;
import java.util.stream.Collectors;

public class PokeSelector {
    private final Map<Species, PokeSpecifics> species;

    public PokeSelector(final String pokeInputWithParameters) throws InstructionParseException {
        if (pokeInputWithParameters.isEmpty())
            throw new InstructionParseException("Invalid PokeSelector, no input was given!");
        this.species = processPokes(new HashMap<>(), pokeInputWithParameters);
    }

    private boolean isEggGroup(String eggGroup) {
        try {
            EggGroup.valueOf(eggGroup.toUpperCase());
        } catch (IllegalArgumentException exception) {
            return false;
        }
        return true;
    }

    // Recursion yay!
    private Map<Species, PokeSpecifics> processPokes(Map<Species, PokeSpecifics> pokesToAdd, String pokesWithParams) throws InstructionParseException {
        final int endingIndex = StringUtils.NextParamIndex(pokesWithParams);
        Set<Species> speciesToAdd;
        PokeSpecifics pokeSpecifics = new PokeSpecifics();
        // According to @getNextParamIndex() our endingIndex should only be bigger if there is nothing left to parse.
        boolean isEndOfLoop = endingIndex == -1;
        if (isEndOfLoop)
            return pokesToAdd;
        String currentPokewithParams = pokesWithParams;
        String nextPokewithParams = "";
        if (!(currentPokewithParams.length() > 0 && currentPokewithParams.length() == endingIndex)) {
            currentPokewithParams = pokesWithParams.substring(0, endingIndex + 1);
            nextPokewithParams = pokesWithParams.substring(endingIndex + 1);
        }
        if (nextPokewithParams.startsWith(","))
            nextPokewithParams = nextPokewithParams.replaceFirst(",", "");
        final int openBracketIndex = currentPokewithParams.indexOf("[");
        // If no open brackets are found we'll assume a pokemon without specifiers is given.
        if (openBracketIndex == -1){
            currentPokewithParams = currentPokewithParams.replace(",", "");
            speciesToAdd = getSpeciesOrGroup(currentPokewithParams);
        } else {
            String pokeFound = currentPokewithParams.substring(0, openBracketIndex);
            String pokeParams = currentPokewithParams.substring(openBracketIndex + 1, currentPokewithParams.length() - 1);

            speciesToAdd = getSpeciesOrGroup(pokeFound);
            pokeSpecifics.parseString(pokeSpecifics, pokeParams);
        }
        speciesToAdd.forEach(spec -> pokesToAdd.put(spec, pokeSpecifics));
        return processPokes(pokesToAdd, nextPokewithParams);
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

    public boolean matches(Pokemon pokemonToCheck) {
        return species.entrySet()
                .stream()
                .anyMatch(speciesEntry -> speciesEntry.getKey().equals(pokemonToCheck.getSpecies())
                        && speciesEntry.getValue().matches(pokemonToCheck));
    }

}
