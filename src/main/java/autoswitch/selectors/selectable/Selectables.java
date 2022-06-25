package autoswitch.selectors.selectable;

import autoswitch.AutoSwitch;

import autoswitch.selectors.futures.IdentifiedTag;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tag.TagKey;

import java.security.InvalidParameterException;
import java.util.Optional;

@SuppressWarnings("unchecked")
public final class Selectables {
    public static void registerVanilla() {
        register(new Selectable.SelectableItem<Item>(Item.class, Item::getMaxDamage,
                                                     //todo leave maxDamage to ItemStack for polymer?
                                                     (i, tag) -> IdentifiedTag.makeItemPredicate((TagKey<Item>) tag).test(i)));

        //todo how to handle polymer entities?
        //  on entity get, see if polymer and then just ignore group? probably can't trust group
        register(new Selectable.SelectableEntity<Entity, EntityType<?>>(Entity.class::isInstance,
                                                                        (Class<EntityType<?>>) (Class<?>) EntityType.class,
                                                                        Entity::getType,
                                                                        entity -> entity instanceof LivingEntity
                                                                                  ? ((LivingEntity) entity).getGroup()
                                                                                  : entity));

        //todo what fallback should polymer get for materials? a targetgroup of the minable tag its in?
        register(new Selectable.SelectableBlock<BlockState, Block>(BlockState.class::isInstance, Block.class,
                                                                   AbstractBlock.AbstractBlockState::getBlock,
                                                                   AbstractBlock.AbstractBlockState::getMaterial,
                                                                   BlockState::isToolRequired,
                                                                   ItemStack::isSuitableFor,
                                                                   ItemStack::getMiningSpeedMultiplier));

        register(new Selectable.SelectableEnchant<Enchantment>(Enchantment.class,
                                                               ((enchantment, tagKey) -> IdentifiedTag
                                                                       .makeEnchantmentPredicate((TagKey<Enchantment>) tagKey)
                                                                       .test(enchantment))));



        registerProcessor(new Selectable.StackProcessor<Item>(ItemStack.class::isInstance, Item.class,
                                                              ItemStack::getItem));
    }

    public static <I> Selectable.StackProcessor<I> getStackProcessor(Object o) {
        for (Selectable.StackProcessor<?> processor : Selectable.PROCESSORS) {
            if (processor.canHandle(o)) {
                return (Selectable.StackProcessor<I>) processor;
            }
        }

        AutoSwitch.logger.info("Failed to find a valid stack processor for the given stack {}", o);
        throw new InvalidParameterException("Missing processor!");
    }

    @SuppressWarnings("unchecked")
    public static <I> Optional<Selectable.SelectableItem<I>> getSelectableItem(I o) {
        for (Selectable<?> selectable : Selectable.SELECTABLE_SET) {
            if (selectable instanceof Selectable.SelectableItem<?> selectableItem) {
                if (selectableItem.canHandle(o)) {
                    return Optional.of((Selectable.SelectableItem<I>) selectableItem);
                }
            }
        }

        return Optional.empty();
    }

    @SuppressWarnings("unchecked")
    public static <I> Optional<Selectable.SelectableEnchant<I>> getSelectableEnchant(I o) {
        for (Selectable<?> selectable : Selectable.SELECTABLE_SET) {
            if (selectable instanceof Selectable.SelectableEnchant<?> selectableEnchant) {
                if (selectableEnchant.canHandle(o)) {
                    return Optional.of((Selectable.SelectableEnchant<I>) selectableEnchant);
                }
            }
        }

        return Optional.empty();
    }

    @SuppressWarnings("unchecked")
    public static <S, B> Optional<Selectable.SelectableBlock<S, B>> getSelectableBlock(S o) {
        for (Selectable<?> selectable : Selectable.SELECTABLE_SET) {
            if (selectable instanceof Selectable.SelectableBlock<?, ?> selectableBlock) {
                if (selectableBlock.canHandle(o)) {
                    return Optional.of((Selectable.SelectableBlock<S, B>) selectableBlock);
                }
            }
        }

        return Optional.empty();
    }

    @SuppressWarnings("unchecked")
    public static <E, T> Optional<Selectable.SelectableEntity<E, T>> getSelectableEntity(E o) {
        for (Selectable<?> selectable : Selectable.SELECTABLE_SET) {
            if (selectable instanceof Selectable.SelectableEntity<?, ?> selectableEntity) {
                if (selectableEntity.canHandle(o)) {
                    return Optional.of((Selectable.SelectableEntity<E, T>) selectableEntity);
                }
            }
        }

        return Optional.empty();
    }

    @SuppressWarnings("unchecked")
    public static <A, B> Optional<Selectable.SelectableTarget<A, B>> getSelectableTarget(A o) {
        for (Selectable<?> selectable : Selectable.SELECTABLE_SET) {
            if (selectable instanceof Selectable.SelectableTarget<?, ?> selectableEntity) {
                if (selectableEntity.canHandle(o)) {
                    return Optional.of((Selectable.SelectableTarget<A, B>) selectableEntity);
                }
            }
        }

        return Optional.empty();
    }

    public static void register(Selectable<?> selectable) {
        Selectable.SELECTABLE_SET.add(selectable);
    }

    public static void registerProcessor(Selectable.StackProcessor<?> processor) {
        Selectable.PROCESSORS.addFirst(processor);
    }

}
