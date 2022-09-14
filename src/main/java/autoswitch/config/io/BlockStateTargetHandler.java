package autoswitch.config.io;

import java.util.function.Predicate;
import java.util.regex.Pattern;

import autoswitch.AutoSwitch;
import autoswitch.selectors.TargetableGroup;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.block.BlockState;
import net.minecraft.command.argument.BlockArgumentParser;
import net.minecraft.command.argument.BlockStateArgumentType;
import net.minecraft.state.property.Property;
import net.minecraft.util.registry.Registry;

public class BlockStateTargetHandler {
    // Does not except DataValues as need to get the ElockEntity
    // With data: ((?<Id>(?:\w+:)?\w+)(?:(?<States>\[((?:\w+\s*=\s*\w+,?\s*)+)\])?)(?:(?<Data>\{((?:\w+\s*:\s*\w+,
    // ?\s*)+)\}))?)
    private static final Pattern groupPattern =
            Pattern.compile("((?<Id>(?:\\w+:)?\\w+)(?:(?<States>\\[((?:\\w+\\s*=\\s*\\w+,?\\s*)+)\\])?))");

    private static boolean isTaggedMatch(String str) {
        return groupPattern.matcher(str).matches();
    }

    /**
     * @see BlockStateArgumentType#parse(StringReader) or fill command
     */
    public static TargetableGroup<BlockState> blockStateTargetGroup(String input) {
        input = input.replaceAll("\\+", "=");
        if (isTaggedMatch(input)) {
            try {
                //todo FREs/RegistryHolder?
                //todo use block predicate instead, as it does not need to specify all states
                //todo link in config header https://minecraft.fandom.com/wiki/Argument_types#block_predicate
                BlockArgumentParser.BlockResult result = BlockArgumentParser.block(Registry.BLOCK, input, false);

                var blockPredicate = (Predicate<Object>) (o) -> {
                    if (o instanceof BlockState state) {
                        var targetState = result.blockState();
                        if (!state.isOf(targetState.getBlock())) return false;

                        for(Property<?> property : result.properties().keySet()) {
                            if (state.get(property) != targetState.get(property)) {
                                return false;
                            }
                        }

                        return true;
                    }

                    return false;
                };

                return new TargetableGroup<>(input, new TargetableGroup.TargetPredicate(input, blockPredicate));
            } catch (CommandSyntaxException e) {
                AutoSwitch.logger.error("BlockState parser", e);
            }
        }

        return null;
    }
}
