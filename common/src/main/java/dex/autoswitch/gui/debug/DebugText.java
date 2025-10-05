package dex.autoswitch.gui.debug;

import dex.autoswitch.mixin.mixins.DebugScreenEntriesAccessor;
import org.jetbrains.annotations.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.debug.DebugScreenDisplayer;
import net.minecraft.client.gui.components.debug.DebugScreenEntry;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;

public class DebugText {
    public static void register() {
        DebugScreenEntriesAccessor.callRegister(
                ResourceLocation.fromNamespaceAndPath("autoswitch", "enchantments"),
                new EnchantmentHelp());
        DebugScreenEntriesAccessor.callRegister(
                ResourceLocation.fromNamespaceAndPath("autoswitch", "item_components"),
                new ItemComponentHelp());
    }

    private record EnchantmentHelp() implements DebugScreenEntry {
        private static final ResourceLocation GROUP_ONE =
                ResourceLocation.fromNamespaceAndPath("autoswitch", "enchantment_help_one");

        @Override
        public void display(DebugScreenDisplayer displayer,
                            @Nullable Level level, @Nullable LevelChunk levelChunk, @Nullable LevelChunk levelChunk1) {
            if (level == null || Minecraft.getInstance().player == null) return;

            var player = Minecraft.getInstance().player;
            var heldItem = player.getMainHandItem();

            if (heldItem.isEnchanted()) {
                displayer.addToGroup(GROUP_ONE, "Held Item Enchantments:");

                var enchantments = heldItem.getEnchantments();
                for (var enchantment : enchantments.keySet()) {
                    displayer.addToGroup(GROUP_ONE, enchantment.getRegisteredName());
                }
            }
        }
    }

    private record ItemComponentHelp() implements DebugScreenEntry {
        private static final ResourceLocation GROUP_ONE =
                ResourceLocation.fromNamespaceAndPath("autoswitch", "component_help_one");

        @Override
        public void display(DebugScreenDisplayer displayer,
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
}
