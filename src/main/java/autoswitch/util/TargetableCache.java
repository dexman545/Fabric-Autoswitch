package autoswitch.util;

import it.unimi.dsi.fastutil.objects.Reference2IntLinkedOpenHashMap;

/**
 * With the new way of triggering switches (not FAPI), the eval. ran constantly when the keys were held.
 * This led to a lot of lag. The cache was added to save recalculating switches.
 * It is designed to have a maximum size, discarding the oldest added element, and to have entries removed based on
 * the slot it was mapping to, allowing for more targeted recalculation.
 */
public class TargetableCache extends Reference2IntLinkedOpenHashMap<Object> {

    private final int maxSize;

    public TargetableCache(int maxSize) {
        super(maxSize);
        this.maxSize = maxSize;
    }

    @Override
    public int put(Object o, int v) {
        if (!this.isEmpty() && this.maxSize <= this.size + 1) this.removeFirstInt();
        return super.put(o, v);
    }

    public void removePairOnValue(final int v) {
        final int[] value = this.value;
        final Object[] key = this.key;
        for (int i = n; i-- != 0; ) {
            if (!((key[i]) == null) && ((value[i]) == (v))) {
                this.remove(key[i], v);
            }
        }
    }

    public int getMaxSize() {
        return maxSize;
    }
}
