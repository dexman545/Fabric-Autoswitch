package dex.autoswitch.debug.gui;

import java.util.Collection;
import java.util.Objects;

import dex.autoswitch.Constants;
import dex.autoswitch.engine.Action;
import dex.autoswitch.engine.Matcher;
import dex.autoswitch.engine.Selector;
import dex.autoswitch.engine.data.Match;
import dex.autoswitch.engine.data.SelectionContext;
import dex.autoswitch.mixin.mixins.DebugScreenEntriesAccessor;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.ObjectReferencePair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.debug.DebugScreenDisplayer;
import net.minecraft.client.gui.components.debug.DebugScreenEntry;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;

public class DebugText {
    public static void register() {
        register("enchantments", new EnchantmentHelp(false));
        register("enchantment_tags", new EnchantmentHelp(true));
        register("item_components", new ItemComponentHelp());
        register("attack_targets", new DebugText.TargetHelp(Action.ATTACK));
        register("interact_targets", new DebugText.TargetHelp(Action.INTERACT));
        register("tool_selectors", new ToolSelectorHelp());
        register("item_tags", new ItemTagHelp());
        register("entity_tags", new EntityTagHelp());
    }

    private static void register(String id, DebugScreenEntry entry) {
        DebugScreenEntriesAccessor.callRegister(ResourceLocation.fromNamespaceAndPath("autoswitch", id), entry);
    }

    private record EnchantmentHelp(boolean showTags) implements DebugScreenEntry {
        private static final ResourceLocation GROUP_ONE =
                ResourceLocation.fromNamespaceAndPath("autoswitch", "enchantment_help_one");
        private static final ResourceLocation GROUP_TWO =
                ResourceLocation.fromNamespaceAndPath("autoswitch", "enchantment_help_two");

        @Override
        public void display(@NotNull DebugScreenDisplayer displayer,
                            @Nullable Level level, @Nullable LevelChunk levelChunk, @Nullable LevelChunk levelChunk1) {
            if (level == null || Minecraft.getInstance().player == null) return;

            var player = Minecraft.getInstance().player;
            var heldItem = player.getMainHandItem();

            if (heldItem.isEnchanted()) {
                displayer.addToGroup(showTags ? GROUP_TWO : GROUP_ONE, "Held Item Enchantments:");

                var enchantments = heldItem.getEnchantments();
                for (var enchantment : enchantments.keySet()) {
                    if (showTags) {
                        enchantment.tags().forEach(tag ->
                                displayer.addToGroup(GROUP_TWO, "#" + tag.location()));
                    } else {
                        displayer.addToGroup(GROUP_ONE, enchantment.getRegisteredName());
                    }
                }
            }
        }
    }

    private record ItemTagHelp() implements DebugScreenEntry {
        private static final ResourceLocation GROUP_ONE =
                ResourceLocation.fromNamespaceAndPath("autoswitch", "item_tag_help_one");

        @Override
        public void display(@NotNull DebugScreenDisplayer displayer,
                            @Nullable Level level, @Nullable LevelChunk levelChunk, @Nullable LevelChunk levelChunk1) {
            if (level == null || Minecraft.getInstance().player == null) return;

            var player = Minecraft.getInstance().player;
            var heldItem = player.getMainHandItem();

            displayer.addToGroup(GROUP_ONE, "Held Item Tags:");

            heldItem.getTags().forEach(tag -> displayer.addToGroup(GROUP_ONE, "#" + tag.location()));
        }
    }

    private record EntityTagHelp() implements DebugScreenEntry {
        private static final ResourceLocation GROUP_ONE =
                ResourceLocation.fromNamespaceAndPath("autoswitch", "entity_tag_help_one");

        @Override
        public void display(@NotNull DebugScreenDisplayer displayer,
                            @Nullable Level level, @Nullable LevelChunk levelChunk, @Nullable LevelChunk levelChunk1) {
            if (level == null || Minecraft.getInstance().player == null) return;

            if (Minecraft.getInstance().hitResult instanceof EntityHitResult result) {
                displayer.addToGroup(GROUP_ONE, "Targeted Entity Tags:");

                //noinspection deprecation
                result.getEntity().getType().builtInRegistryHolder().tags()
                        .forEach(tag -> displayer.addToGroup(GROUP_ONE, "#" + tag.location()));
            }
        }
    }

    private record ItemComponentHelp() implements DebugScreenEntry {
        private static final ResourceLocation GROUP_ONE =
                ResourceLocation.fromNamespaceAndPath("autoswitch", "component_help_one");

        @Override
        public void display(@NotNull DebugScreenDisplayer displayer,
                            @Nullable Level level, @Nullable LevelChunk levelChunk, @Nullable LevelChunk levelChunk1) {
            if (level == null || Minecraft.getInstance().player == null) return;

            var player = Minecraft.getInstance().player;

            var heldItem = player.getMainHandItem();
            var components = heldItem.getComponents();

            if (components.has(DataComponents.POTION_CONTENTS)) {
                displayer.addToGroup(GROUP_ONE, "Held Item Potion Contents:");
                var pc = components.getTyped(DataComponents.POTION_CONTENTS);
                if (pc == null) return;
                displayer.addToGroup(GROUP_ONE, pc.value().potion()
                        .map(Holder::getRegisteredName).orElse("unknown"));
            }
        }
    }

    private record TargetHelp(Action action) implements DebugScreenEntry {
        private static final ResourceLocation GROUP_ONE =
                ResourceLocation.fromNamespaceAndPath("autoswitch", "target_help_one");
        private static final ResourceLocation GROUP_TWO =
                ResourceLocation.fromNamespaceAndPath("autoswitch", "target_help_two");

        @Override
        public void display(@NotNull DebugScreenDisplayer displayer,
                            @Nullable Level level, @Nullable LevelChunk levelChunk, @Nullable LevelChunk levelChunk1) {
            if (level == null || Minecraft.getInstance().player == null || Minecraft.getInstance().hitResult == null) {
                return;
            }

            var selectors = Constants.CONFIG.getConfiguration().get(action);
            var context = switch (Objects.requireNonNull(Minecraft.getInstance().hitResult.getType())) {
                case MISS -> null;
                case BLOCK -> {
                    if (Minecraft.getInstance().hitResult instanceof BlockHitResult result) {
                        assert Minecraft.getInstance().level != null;
                        var block = Minecraft.getInstance().level.getBlockState(result.getBlockPos());
                        yield new SelectionContext(action, block);
                    }
                    yield null;
                }
                case ENTITY -> {
                    if (Minecraft.getInstance().hitResult instanceof EntityHitResult result) {
                        yield new SelectionContext(action, result.getEntity());
                    }
                    yield null;
                }
            };

            if (context == null) return;

            var targets = selectors.keySet().stream()
                    .map(selector -> {
                        var m = selector.matches(context, context.target());
                        return ObjectReferencePair.<Selector, Match>of(selector, m);
                    })
                    .filter(p -> p.right().matches())
                    .sorted((c2, c1) -> {
                        // Target priority
                        int diff = Integer.compare(c1.left().priority(), c2.left().priority());
                        if (diff != 0) return diff;

                        // Target rating levels
                        var maxTargetRatingLevel = Math.max(c1.right().getMaxLevel(), c2.right().getMaxLevel());
                        for (int i = 0; i <= maxTargetRatingLevel; i++) {
                            var r1 = c1.right().getRating(i);
                            var r2 = c2.right().getRating(i);
                            diff = Double.compare(r1, r2);
                            if (diff != 0) return diff;
                        }

                        return 0;
                    })
                    .map(Pair::left)
                    .map(s -> Matcher.prettyPrint(s.matcher()))
                    .flatMap(String::lines)
                    .toList();

            if (targets.isEmpty()) return;

            var group = switch (action) {
                case ATTACK -> GROUP_TWO;
                case INTERACT -> GROUP_ONE;
                default -> throw new IllegalStateException("Unexpected value: " + action);
            };

            displayer.addToGroup(group, "Matched Targets for %s:".formatted(action.name()));
            for (String target : targets) {
                displayer.addToGroup(group, target);
            }
        }
    }

    private record ToolSelectorHelp() implements DebugScreenEntry {
        private static final ResourceLocation GROUP_ONE =
                ResourceLocation.fromNamespaceAndPath("autoswitch", "item_selector_help_one");
        @Override
        public void display(@NotNull DebugScreenDisplayer displayer,
                            @Nullable Level level, @Nullable LevelChunk levelChunk, @Nullable LevelChunk levelChunk1) {
            if (level == null || Minecraft.getInstance().player == null) return;

            var player = Minecraft.getInstance().player;
            var heldItem = player.getMainHandItem();

            var context = new SelectionContext(Action.INTERACT, heldItem);

            var selectors = Constants.CONFIG.getConfiguration().values().stream()
                    .flatMap(s -> s.values().stream())
                    .flatMap(Collection::stream)
                    .map(selector -> {
                        var m = selector.matches(context, context.target());
                        return ObjectReferencePair.<Selector, Match>of(selector, m);
                    })
                    .filter(p -> p.right().matches())
                    .sorted((c2, c1) -> {
                        var diff = 0;
                        // Tool rating levels
                        var maxToolRatingLevel = Math.max(c1.right().getMaxLevel(), c2.right().getMaxLevel());
                        for (int i = 0; i <= maxToolRatingLevel; i++) {
                            var r1 = c1.right().getRating(i);
                            var r2 = c2.right().getRating(i);
                            diff = Double.compare(r1, r2);
                            if (diff != 0) return diff;
                        }

                        return 0;
                    })
                    .map(Pair::left)
                    .map(Selector::matcher)
                    .distinct()
                    .map(Matcher::prettyPrint)
                    .flatMap(String::lines)
                    .toList();

            if (selectors.isEmpty()) return;

            displayer.addToGroup(GROUP_ONE, "Matched Tool Selectors for Held Item");
            for (String target : selectors) {
                displayer.addToGroup(GROUP_ONE, target);
            }
        }
    }
}
