package autoswitch.selectors.selectable;

import it.unimi.dsi.fastutil.objects.Object2BooleanFunction;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

import net.minecraft.item.ItemStack;
import net.minecraft.tag.TagKey;

import java.util.LinkedList;
import java.util.function.BiFunction;
import java.util.function.Function;

public interface Selectable<T> {
    ObjectOpenHashSet<Selectable<?>> SELECTABLE_SET = new ObjectOpenHashSet<>();
    LinkedList<StackProcessor<?>> PROCESSORS = new LinkedList<>();

    boolean canHandle(Object o);

    Class<T> clazz();

    default T safety(Object o) {
        if (clazz().isInstance(o)) {
            return clazz().cast(o);
        }
        return null;
    }

    record StackProcessor<I>(Object2BooleanFunction<Object> canHandle,
                             Class<I> clazz, Function<ItemStack, I> processStack) implements Selectable<I> {
        @Override
        public boolean canHandle(Object o) {
            return canHandle.test(o);
        }
    }

    record SelectableItem<I>(Object2BooleanFunction<Object> canHandle,
                             Class<I> clazz,
                             Function<I, Integer> getMaxDamage,
                             BiFunction<I, TagKey<?>, Boolean> isIn) implements Selectable<I> {
        public SelectableItem(Class<I> clazz,
                              Function<I, Integer> getMaxDamage,
                              //todo damage is on itemstack
                              BiFunction<I, TagKey<?>, Boolean> isIn) {
            this(clazz::isInstance, clazz, getMaxDamage, isIn);
        }

        @Override
        public boolean canHandle(Object o) {
            return canHandle.test(o);
        }
    }

    //todo if these take a stack, how to handle polymer? can't just register a new one? linked list and prepend?
    //todo impl and this, wait and see what polymer does
    record SelectableEnchant<E>(Object2BooleanFunction<Object> canHandle,
                                Class<E> clazz, BiFunction<E, TagKey<?>, Boolean> isIn) implements Selectable<E> {
        public SelectableEnchant(Class<E> clazz, BiFunction<E, TagKey<?>, Boolean> isIn) {
            this(clazz::isInstance, clazz, isIn);
        }

        @Override
        public boolean canHandle(Object o) {
            return canHandle.test(o);
        }
    }

    record SelectableBlock<S, B>(Object2BooleanFunction<Object> canHandle,
                                 Class<B> clazz, Function<S, B> getSpecific,
                                 Function<S, Object> getGroup, Function<S, Boolean> requiresTool,
                                 BiFunction<ItemStack, S, Boolean> isSuitableFor,
                                 BiFunction<ItemStack, S, Float> miningSpeedFactor) implements SelectableTarget<S, B> {
        public SelectableBlock(Class<B> clazz, Function<S, B> getBlock, Function<S, Object> getMaterial,
                               Function<S, Boolean> requiresTool,
                               BiFunction<ItemStack, S, Boolean> isSuitableFor,
                               BiFunction<ItemStack, S, Float> miningSpeedFactor) {
            this(clazz::isInstance, clazz, getBlock, getMaterial, requiresTool, isSuitableFor, miningSpeedFactor);
        }

        @Override
        public boolean canHandle(Object o) {
            return canHandle.test(o);
        }
    }

    record SelectableEntity<E, T>(Object2BooleanFunction<Object> canHandle,
                                Class<T> clazz, Function<E, T> getSpecific,
                               Function<E, Object> getGroup) implements SelectableTarget<E, T> {
        public SelectableEntity(Class<T> clazz, Function<E, T> getType, Function<E, Object> getGroup) {
            this(clazz::isInstance, clazz, getType, getGroup);
        }

        @Override
        public boolean canHandle(Object o) {
            return canHandle.test(o);
        }
    }

    // A -> B
    interface SelectableTarget<A, B> extends Selectable<B> {
        Function<A, B> getSpecific();

        Function<A, Object> getGroup();
    }
}
