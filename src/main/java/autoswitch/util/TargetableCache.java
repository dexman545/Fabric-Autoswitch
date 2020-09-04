package autoswitch.util;

import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2IntLinkedOpenHashMap;

public class TargetableCache extends Reference2IntLinkedOpenHashMap<Object> {

    private final int maxSize;

    public TargetableCache(int maxSize) {
        super(maxSize);
        this.maxSize = maxSize;
    }

    @Override
    public int put(Object o, int v) {
        if (this.maxSize <= this.size + 1) this.removeFirstInt();
        return super.put(o, v);
    }

    public void removePairOnValue(final int v) {
        final int[] value = this.value;
        final Object[] key = this.key;
        for (int i = n; i-- != 0;)
            if (!((key[i]) == null) && ((value[i]) == (v)))
                this.remove(key[i], v);
    }

    public int getMaxSize() {
        return maxSize;
    }
}
