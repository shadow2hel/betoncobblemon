package shadow2hel.betoncobblemon.condition;

import com.cobblemon.mod.common.pokemon.Pokemon;
import io.izzel.arclight.common.mod.server.ArclightServer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import org.betonquest.betonquest.Instruction;
import org.betonquest.betonquest.api.Condition;
import org.betonquest.betonquest.api.profiles.Profile;
import org.betonquest.betonquest.exceptions.InstructionParseException;
import org.betonquest.betonquest.exceptions.QuestRuntimeException;
import shadow2hel.betoncobblemon.util.PokeSelector;

public class ShoulderMountCondition extends Condition {
    private final PokeSelector pokeSelector;

    public ShoulderMountCondition(Instruction instruction) throws InstructionParseException {
        super(instruction, true);
        pokeSelector = new PokeSelector(instruction.next());
    }

    @Override
    protected Boolean execute(Profile profile) throws QuestRuntimeException {
        ServerPlayer player = ArclightServer.getMinecraftServer().getPlayerList().getPlayer(profile.getPlayerUUID());
        if (player == null)
            throw new QuestRuntimeException("Player is not initialized!");
        CompoundTag nbtShoulderEntityLeft = !player.getShoulderEntityLeft().isEmpty() ?
                player.getShoulderEntityLeft().getCompound("Pokemon"): new CompoundTag();
        CompoundTag nbtShoulderEntityReft = !player.getShoulderEntityRight().isEmpty() ?
                player.getShoulderEntityRight().getCompound("Pokemon"): new CompoundTag();
        Pokemon leftShoulderPoke = null;
        Pokemon rightShoulderPoke = null;
        if (!nbtShoulderEntityLeft.isEmpty())
            leftShoulderPoke = new Pokemon().loadFromNBT(player.getShoulderEntityLeft().getCompound("Pokemon"));
        if (!nbtShoulderEntityReft.isEmpty())
            rightShoulderPoke = new Pokemon().loadFromNBT(player.getShoulderEntityRight().getCompound("Pokemon"));
        return pokeSelector.matches(leftShoulderPoke) || pokeSelector.matches(rightShoulderPoke);
    }
}
