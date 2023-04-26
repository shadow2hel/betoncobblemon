package shadow2hel.betoncobblemon.util;

import com.cobblemon.mod.common.api.pokeball.PokeBalls;
import com.cobblemon.mod.common.pokeball.PokeBall;
import com.cobblemon.mod.common.pokemon.Pokemon;
import org.betonquest.betonquest.exceptions.InstructionParseException;

import java.lang.constant.Constable;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

class PokeSpecifics {
    private Integer minLevel;
    private Integer maxLevel;
    private Set<PokeBall> pokeBalls;
    private boolean shinyRequired;

    PokeSpecifics() {
        minLevel = 0;
        maxLevel = 0;
        pokeBalls = new HashSet<>();
        shinyRequired = false;
    }

    PokeSpecifics parseString(PokeSpecifics specifics, String pokeParametersRaw) throws InstructionParseException {
        final int endingIndex = StringUtils.NextParamIndex(pokeParametersRaw);
        boolean runOutOfParams = endingIndex == -1;
        if (runOutOfParams || pokeParametersRaw.equalsIgnoreCase("]"))
            return specifics;

        String currentParam = pokeParametersRaw.substring(0, endingIndex);
        String nextParams = "";
        if (endingIndex < pokeParametersRaw.length())
            nextParams = pokeParametersRaw.substring(endingIndex);
        if (nextParams.startsWith("]"))
            nextParams = nextParams.replaceFirst("]", "");
        if (nextParams.startsWith(","))
            nextParams = nextParams.replaceFirst(",", "");
        boolean isToggle = !currentParam.contains(":");
        if (isToggle) {
            if (currentParam.equalsIgnoreCase("shiny"))
                specifics.requireShiny(true);
            else
                throw new InstructionParseException("Parameter: " + currentParam + " is of unknown type!");
            return parseString(specifics, nextParams);
        }

        String[] paramAndValue = currentParam.split(":");
        String paramKey = paramAndValue[0];
        String paramValue = paramAndValue[1];
        // We do a lil bit of reflecting
        for (Field field : this.getClass().getDeclaredFields()) {
            if (field.getName().equalsIgnoreCase(paramKey)) {
                //field.setAccessible(true);
                if (Constable.class.isAssignableFrom(field.getType())) {

                    if (Integer.class.isAssignableFrom(field.getType())) {
                        try {
                            field.set(this, Integer.parseInt(paramValue));
                        } catch (IllegalAccessException e) {
                            throw new RuntimeException(e);
                        }
                    } else if (String.class.isAssignableFrom(field.getType())) {
                        try {
                            field.set(this, paramValue);
                        } catch (IllegalAccessException e) {
                            throw new RuntimeException(e);
                        }
                    }


                } else if (Collection.class.isAssignableFrom(field.getType())) {
                    String specifiersRaw = paramValue.replace("[", "");
                    specifiersRaw = specifiersRaw.replace("]", "");
                    String[] specifiers = specifiersRaw.split(",");
                    if (field.getName().equalsIgnoreCase("pokeballs")) {
                        Set<PokeBall> ballsToAdd = new HashSet<>();
                        for (String strBall : specifiers) {
                            PokeBalls.INSTANCE.all()
                                    .stream()
                                    .filter(ball -> ball.getName().toString().contains(strBall))
                                    .findAny()
                                    .ifPresent(ballsToAdd::add);
                        }
                        if (ballsToAdd.size() > 0) {
                            specifics.requireSpecificPokeballs(ballsToAdd);
                        } else
                            throw new InstructionParseException("The given pokeballs weren't valid!");
                    }
                }
            }
        }
        return parseString(specifics, nextParams);
    }

    PokeSpecifics requireMinLevel(Integer minLvl) {
        minLevel = minLvl;
        return this;
    }

    PokeSpecifics requireMaxLevel(Integer maxLvl) {
        this.maxLevel = maxLvl;
        return this;
    }

    PokeSpecifics requireSpecificPokeballs(Set<PokeBall> pokeBalls) {
        this.pokeBalls = pokeBalls;
        return this;
    }

    PokeSpecifics requireShiny(boolean shinyRequired) {
        this.shinyRequired = shinyRequired;
        return this;
    }

    boolean matches(Pokemon pokemonToCheck) {
        if (shinyRequired && !pokemonToCheck.getShiny()) {
            return false;
        }
        if (pokeBalls.size() > 0 && !pokeBalls.contains(pokemonToCheck.getCaughtBall()))
            return false;
        return (minLevel <= 0 || minLevel <= pokemonToCheck.getLevel())
                && (maxLevel <= 0 || pokemonToCheck.getLevel() <= maxLevel);
    }
}
