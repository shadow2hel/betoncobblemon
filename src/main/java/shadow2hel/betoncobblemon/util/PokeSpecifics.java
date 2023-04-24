package shadow2hel.betoncobblemon.util;

import com.cobblemon.mod.common.api.pokeball.PokeBalls;
import com.cobblemon.mod.common.pokeball.PokeBall;
import com.cobblemon.mod.common.pokemon.Pokemon;
import org.betonquest.betonquest.exceptions.InstructionParseException;

import java.util.HashSet;
import java.util.Set;

class PokeSpecifics {
    private int minLevelRequired;
    private int maxLevelRequired;
    private Set<PokeBall> pokeBalls;
    private boolean shinyRequired;

    PokeSpecifics() {
        minLevelRequired = 0;
        maxLevelRequired = 0;
        pokeBalls = new HashSet<>();
        shinyRequired = false;
    }


    //TODO Implement comma separators for nested lists, as in @PokeSelector$processPokesAdvanced()
    PokeSpecifics parseString(String pokeParametersRaw) throws InstructionParseException {
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
                        String[] allBalls = value.split("/");
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

    PokeSpecifics requireMinLevel(Integer minLvl) {
        minLevelRequired = minLvl;
        return this;
    }

    PokeSpecifics requireMaxLevel(Integer maxLvl) {
        this.maxLevelRequired = maxLvl;
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
        return (minLevelRequired <= 0 || minLevelRequired <= pokemonToCheck.getLevel())
                && (maxLevelRequired <= 0 || pokemonToCheck.getLevel() <= maxLevelRequired);
    }
}
