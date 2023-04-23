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
        this.species = processPokes(pokeInputWithParameters);
    }

    private boolean isEggGroup(String eggGroup) {
        try {
            EggGroup.valueOf(eggGroup.toUpperCase());
        } catch (IllegalArgumentException exception) {
            return false;
        }
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
            PokeSpecifics specifics = new PokeSpecifics();
            specifics.parseString(pokeParamsRaw);

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

    public boolean matches(Pokemon pokemonToCheck) {
        return species.entrySet()
                .stream()
                .anyMatch(speciesEntry -> speciesEntry.getKey().equals(pokemonToCheck.getSpecies())
                        && speciesEntry.getValue().matches(pokemonToCheck));
    }

}
