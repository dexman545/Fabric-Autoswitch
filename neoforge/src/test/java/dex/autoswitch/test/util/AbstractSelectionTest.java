package dex.autoswitch.test.util;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;

import dex.autoswitch.config.AutoSwitchConfig;
import dex.autoswitch.config.ConfigHandler;
import dex.autoswitch.engine.SelectionEngine;
import dex.autoswitch.engine.data.extensible.PlayerInventory;
import net.neoforged.testframework.junit.EphemeralTestServerProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.opentest4j.MultipleFailuresError;
import org.spongepowered.configurate.ConfigurateException;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

@ExtendWith(EphemeralTestServerProvider.class)
public abstract class AbstractSelectionTest {
    protected SelectionEngine engine;

    @BeforeEach
    void setup() {
        engine = getEngine();
    }

    protected static void assertSelectedSlot(int expectedSlot, PlayerInventory<ItemStack> inventory) {
        try {
            assertTrue(expectedSlot < inventory.slotCount(), "Slot out of bounds!");
            assertAll("Selected Tool Test",
                    () -> assertEquals(expectedSlot, inventory.currentSelectedSlot()),
                    () -> assertEquals(inventory.getTool(expectedSlot), inventory.getTool(inventory.currentSelectedSlot()), () -> {
                        var sb = new StringBuilder("Expected slot %s but got %s!"
                                .formatted(expectedSlot, inventory.currentSelectedSlot()));

                        sb.append("\n\tExpected: ").append(stackString(inventory.getTool(expectedSlot))).append("\n");
                        sb.append("\tSelected: ").append(stackString(inventory.getTool(inventory.currentSelectedSlot())))
                                .append("\n");

                        sb.append("\tInventory:");

                        if (true && inventory.slotCount() > 10) {
                            sb.append("(HIDDEN DUE TO SIZE)");
                        } else {
                            sb.append("\n");
                            for (int slot = 0; slot < inventory.slotCount(); slot++) {
                                sb.append('\t').append('\t').append(stackString(inventory.getTool(slot)));
                                if (expectedSlot == slot) {
                                    sb.append("\t<<<<<< EXPECTED");
                                }
                                if (inventory.currentSelectedSlot() == slot) {
                                    sb.append("\t<<<<<< SELECTED");
                                }
                                sb.append('\n');
                            }
                        }

                        return sb.toString();
                    }));
        } catch (MultipleFailuresError e) {
            throw e;
        }
    }

    protected static ItemStack stack(MinecraftServer server, Item item, Enchant... enchantments) {
        var stack = item.getDefaultInstance();
        if (EnchantmentHelper.canStoreEnchantments(stack)) {
            for (var enchantment : enchantments) {
                if (enchantment != null) {
                    var e = server.registryAccess().lookupOrThrow(Registries.ENCHANTMENT)
                            .getOrThrow(enchantment.key);
                    stack.enchant(e, enchantment.level);
                }
            }
        }

        return stack;
    }

    protected static BlockState block(Block block, State<?, ?>... states) {
        var state = block.defaultBlockState();
        for (@SuppressWarnings("rawtypes") State s : states) {
            //noinspection unchecked
            state = state.setValue(s.property, s.value);
        }

        return state;
    }

    protected static <T extends Entity> T entity(ServerLevel serverLevel, EntityType<T> type) {
        return type.create(serverLevel, EntitySpawnReason.NATURAL);
    }

    private static String stackString(ItemStack stack) {
        var sb = new StringBuilder("{");

        sb.append(stack.getCount());
        sb.append(" ");
        sb.append(stack.getItem());
        sb.append(" ");

        var ens = stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        sb.append(ens);

        return sb.append("}").toString();
    }

    public abstract SelectionEngine getEngine();

    protected AutoSwitchConfig loadConfig(String file) {
        try {
            var p = Path.of("resources", "test", "configs", file + ".conf");
            var realPath = Path.of(System.getProperty("user.dir")).getParent().resolve(p);
            assertTrue(Files.exists(realPath), () -> realPath.toAbsolutePath().toString());
            return ConfigHandler.readConfiguration(realPath);
        } catch (ConfigurateException e) {
            throw new RuntimeException(e);
        }
    }

    protected record Enchant(ResourceKey<Enchantment> key, int level) {
        public static Enchant of(ResourceKey<Enchantment> key, int level) {
            return new Enchant(key, level);
        }
    }

    protected record State<T extends Comparable<T>, V extends T>(Property<T> property, V value) {
        public static <T extends Comparable<T>, V extends T> State<T, V> of(Property<T> property, V value) {
            return new State<>(property, value);
        }
    }
}
