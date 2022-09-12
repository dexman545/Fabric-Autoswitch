package autoswitch.config.io;

import java.util.regex.Pattern;

import autoswitch.AutoSwitch;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.block.BlockState;
import net.minecraft.command.argument.BlockArgumentParser;
import net.minecraft.command.argument.BlockStateArgumentType;
import net.minecraft.util.registry.Registry;

public class BlockStateTargetHandler {
    private static final Pattern groupPattern =
            Pattern.compile("((\\w+:)?\\w+((\\[(\\w+\\s*=[\\w\\d,\\s]+)+\\])|(\\{(\\w+\\s*:[\\w\\d,\\s]+)+\\})){1,2})");

    private static boolean isTaggedMatch(String str) {
        return groupPattern.matcher(str).matches();
    }

    /**
     * @see BlockStateArgumentType#parse(StringReader) or fill command
     */
    public static BlockState getBlockState(String input) {
        input = input.replaceAll("\\+", "=");
        if (isTaggedMatch(input)) {
            try {
                //todo FREs/RegistryHolder?
                BlockArgumentParser.BlockResult blockResult = BlockArgumentParser.block(Registry.BLOCK, input, true);
                return blockResult.blockState();
            } catch (CommandSyntaxException e) {
                AutoSwitch.logger.error("BlockState parser", e);
            }
        }

        return null;
    }
}
