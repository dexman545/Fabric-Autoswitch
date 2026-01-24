package dex.autoswitch.engine.types.selectable;

import java.util.function.Predicate;

import dex.autoswitch.Constants;
import dex.autoswitch.api.impl.AutoSwitchApi;
import dex.autoswitch.engine.Action;
import dex.autoswitch.engine.TargetType;
import dex.autoswitch.engine.data.SelectionContext;
import dex.autoswitch.futures.FutureSelectable;
import dex.autoswitch.platform.Services;
import org.apache.commons.lang3.mutable.MutableDouble;
import org.jetbrains.annotations.Nullable;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.block.state.BlockState;

public class ItemSelectableType extends SelectableResource<Item> {
    public static final ItemSelectableType INSTANCE = new ItemSelectableType();

    public ItemSelectableType() {
        super("item");
    }

    @Override
    public Holder<Item> lookup(Identifier identifier) {
        return BuiltInRegistries.ITEM.get(identifier).orElse(null);
    }

    @Override
    public TagKey<Item> lookupGroup(Identifier identifier) {
        return TagKey.create(Registries.ITEM, identifier);
    }

    @Override
    public boolean matches(SelectionContext context, Holder<Item> v, Object selectable) {
        var ref = v.value();
        if (selectable instanceof ItemStack stack) {
            if (Constants.CONFIG.featureConfig.skipDepletedItems) {
                for (Predicate<ItemStack> stackPredicate : AutoSwitchApi.INSTANCE.DEPLETED) {
                    if (stackPredicate.test(stack)) {
                        return false;
                    }
                }
            }

            if (Constants.CONFIG.featureConfig.preserveDamagedTools) {
                if (stack.isDamageableItem() && stack.nextDamageWillBreak()) {
                    return false;
                }
            }

            if (ref.equals(stack.getItem())) {
                // Check for mining level
                if (context.target() instanceof BlockState state) {
                    return isCorrectTool(stack, state);
                }

                return true;
            }
        }

        if (selectable instanceof Item item) {
            if (ref.equals(item)) {
                // Check for mining level
                if (context.target() instanceof BlockState state) {
                    return isCorrectTool(item, state);
                }

                return true;
            }
        }

        return false;
    }

    @Override
    public boolean matchesGroup(SelectionContext context, TagKey<Item> itemTagKey, Object selectable) {
        if (selectable instanceof ItemStack stack) {
            if (Constants.CONFIG.featureConfig.skipDepletedItems) {
                for (Predicate<ItemStack> stackPredicate : AutoSwitchApi.INSTANCE.DEPLETED) {
                    if (stackPredicate.test(stack)) {
                        return false;
                    }
                }
            }

            if (Constants.CONFIG.featureConfig.preserveDamagedTools) {
                if (stack.isDamageableItem() && stack.nextDamageWillBreak()) {
                    return false;
                }
            }

            if (Services.PLATFORM.isInTag(itemTagKey, stack.getItem())) {
                // Check for mining level
                if (context.target() instanceof BlockState state) {
                    return isCorrectTool(stack, state);
                }

                return true;
            }
        }

        if (selectable instanceof Item item) {
            if (Services.PLATFORM.isInTag(itemTagKey, item)) {
                // Check for mining level
                if (context.target() instanceof BlockState state) {
                    return isCorrectTool(item, state);
                }

                return true;
            }
        }

        return false;
    }

    private boolean isCorrectTool(ItemStack stack, BlockState state) {
        return !state.requiresCorrectToolForDrops() || stack.isCorrectToolForDrops(state);
    }

    private boolean isCorrectTool(Item item, BlockState state) {
        var tool = item.components().get(DataComponents.TOOL);
        if (!state.requiresCorrectToolForDrops()) {
            return true;
        }

        return tool != null && tool.isCorrectForDrops(state);
    }

    @Override
    public @Nullable TargetType targetType() {
        return null;
    }

    @Override
    public boolean isOf(Object o) {
        return o instanceof ItemStack || o instanceof Item || (o instanceof Holder<?> h && h.value() instanceof Item);
    }

    /**
     * For {@link Entity} damage, see {@link Mob#doHurtTarget(ServerLevel, Entity)} and {@link Player#attack(Entity)}
     * See {@link ItemAttributeModifiers#compute(Holder, double, EquipmentSlot)} for attribute calculation
     * See {@link Player#createAttributes()} for default attribute values
     */
    @Override
    public double typeRating(SelectionContext context, FutureSelectable<Identifier, Holder<Item>> futureValue, Object selectable) {
        if (context.action() != Action.ATTACK) {
            if (selectable instanceof ItemStack stack) {
                if (stack.isDamageableItem()) {
                    if (stack.getMaxDamage() > 0) {
                        return (double) stack.getDamageValue() / stack.getMaxDamage();
                    }
                } else {
                    if (stack.isStackable()) {
                        return (double) stack.getCount() / stack.getMaxStackSize();
                    }
                }
            }

            return 0;
        }

        if (selectable instanceof ItemStack stack) {
            if (context.target() instanceof BlockState state) {
                var tool = stack.get(DataComponents.TOOL);

                var defaultSpeed = 1D;
                if (tool != null) {
                    defaultSpeed = tool.defaultMiningSpeed();
                }

                var normSpeed = (stack.getDestroySpeed(state) - defaultSpeed) / defaultSpeed;

                if (Constants.CONFIG.featureConfig.preferMinimumViableTool) {
                    normSpeed = 1 - normSpeed;
                }

                return normSpeed;
            }

            if (context.target() instanceof Entity entity) {
                var damage = new MutableDouble(1);
                var speed = new MutableDouble(4);
                stack.forEachModifier(EquipmentSlot.MAINHAND, (attributeHolder, attributeModifier) -> {
                    //noinspection deprecation
                    if (attributeHolder.is(Attributes.ATTACK_DAMAGE)) {
                        var baseValue = 1;
                        double d1 = attributeModifier.amount();

                        var d0 = damage.doubleValue();
                        d0 += switch (attributeModifier.operation()) {
                            case ADD_VALUE -> d1;
                            case ADD_MULTIPLIED_BASE -> d1 * baseValue;
                            case ADD_MULTIPLIED_TOTAL -> d1 * d0;
                        };
                        damage.setValue(d0);
                    }

                    //noinspection deprecation
                    if (attributeHolder.is(Attributes.ATTACK_SPEED)) {
                        var baseValue = 4;
                        double d1 = attributeModifier.amount();

                        var d0 = speed.doubleValue();
                        d0 += switch (attributeModifier.operation()) {
                            case ADD_VALUE -> d1;
                            case ADD_MULTIPLIED_BASE -> d1 * baseValue;
                            case ADD_MULTIPLIED_TOTAL -> d1 * d0;
                        };
                        speed.setValue(d0);
                    }
                });

                if (speed.doubleValue() == 0 || damage.doubleValue() == 0) return 0;

                // DPS is just attack speed * damage
                // Attack speed * damage lines up with DPS on the wiki
                // See table https://minecraft.wiki/w/Damage#Dealing_damage
                return (damage.doubleValue() * speed.doubleValue());
            }
        }

        return 0;
    }

    /**
     * Function has a maximum of 1 at e; is 0 at 1; decays from e -> infinity. The (1/.16) is correction factor to have
     * maximum output be 1, instead of 0.16
     * <p>
     * Default output for getMiningSpeed is 1, same speed as the player's hand. This function clamps that to 0, as it is
     * not a viable tool under normal circumstances.
     *
     * @param original original rating
     *
     * @return clamped rating if needed
     */
    private static float clampMiningSpeed(float original) {
        if (original > 0) {
            return (float) ((1 / .16) * Math.log10(original) / original);
        }

        return original;
    }
}
