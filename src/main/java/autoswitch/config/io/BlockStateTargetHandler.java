package autoswitch.config.io;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import autoswitch.selectors.TargetableGroup;
import autoswitch.selectors.futures.FutureRegistryEntry;
import autoswitch.selectors.futures.RegistryType;

import com.mojang.brigadier.StringReader;

import net.minecraft.commands.arguments.blocks.BlockStateArgument;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

public class BlockStateTargetHandler {
    private static final Pattern groupPattern =
            Pattern.compile("((?<Id>(?:\\w+:)?\\w+)(?:(?<States>\\[((?:\\w+\\s*=\\s*\\w+,?\\s*)+)\\])?)(?:(?<Data>\\{" +
                            "((?:\\w+\\s*:\\s*\\w+,?\\s*)+)\\}))?)");

    private static Match isTaggedMatch(String str) {
        var match = groupPattern.matcher(str);

        if (match.matches() && match.group("States") != null) {
            return new Match(match.matches(), match.group("Id"), match.group("States")
                                                                      .replace("[", "")
                                                                      .replace("]", ""));
        }

        return new Match(false, null, null);
    }

    /**
     * @see BlockStateArgument#parse(StringReader) or fill command
     */
    public static TargetableGroup<BlockState> blockStateTargetGroup(String input) {
        input = input.replaceAll("\\+", "=");

        var match = isTaggedMatch(input);
        if (match.isMatch) {
            var futureBlock = FutureRegistryEntry.getOrCreate(RegistryType.BLOCK, new ResourceLocation(match.id));
            futureBlock.setTypeLocked(true);

            if (match.properties == null) return null;

            var properties =
                    Arrays.stream(match.properties().split(",")).map(String::strip)
                          .map(s -> s.split("=")).filter(a -> a.length == 2)
                          .collect(Collectors.toMap(e -> e[0], e -> e[1]));

            var blockPredicate = (Predicate<Object>) (o) -> {
                if (o instanceof BlockState state) {
                    if (!futureBlock.matches(state.getBlock())) return false;

                    for(Map.Entry<String, String> entry : properties.entrySet()) {
                        Property<?> property = state.getBlock().getStateDefinition().getProperty(entry.getKey());
                        if (property == null) {
                            return false;
                        }

                        Comparable<?> comparable = property.getValue(entry.getValue()).orElse(null);
                        if (comparable == null) {
                            return false;
                        }

                        if (state.getValue(property) != comparable) {
                            return false;
                        }
                    }

                    return true;
                }

                return false;
            };

            return new TargetableGroup<>(input, new TargetableGroup.TargetPredicate(input, blockPredicate));
        }

        return null;
    }

    private record Match(boolean isMatch, String id, String properties) {}
}
