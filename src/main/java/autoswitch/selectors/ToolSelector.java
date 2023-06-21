package autoswitch.selectors;

import java.util.Arrays;
import java.util.Locale;

import autoswitch.AutoSwitch;
import autoswitch.config.io.TagTargetHandler;

import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import org.jetbrains.annotations.Nullable;

import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public class ToolSelector implements Selector<ItemStack> {
    private final ItemSelector itemSelector;
    private final EnchantmentSelector[] enchantmentSelectors;
    private final int id;

    public ToolSelector(ItemSelector itemSelector, @Nullable EnchantmentSelector... enchantmentSelectors) {
        this.itemSelector = itemSelector;
        this.enchantmentSelectors = enchantmentSelectors;

        // Skip the rest of the evaluation as the ToolSelector has already been handled
        if (AutoSwitch.switchData.toolSelectorKeys.containsKey(configEntry())) {
            this.id = AutoSwitch.switchData.toolSelectorKeys.getInt(configEntry());
            return;
        }

        this.id = makeId();
    }

    public ToolSelector(String input) {
        input = input.toLowerCase(Locale.ENGLISH).trim().replace("!", ":");

        // Skip the rest of the evaluation as the ToolSelector has already been handled
        if (AutoSwitch.switchData.toolSelectorKeys.containsKey(input)) {
            var old = AutoSwitch.switchData.toolSelectors.get(AutoSwitch.switchData.toolSelectorKeys.getInt(input));
            this.itemSelector = old.itemSelector;
            this.enchantmentSelectors = old.enchantmentSelectors;
            this.id = old.id;
            return;
        }

        String[] cleanedInput = input.split(";");
        String itemSelectorStr = cleanedInput[0].trim();
        String enchantmentsStr = cleanedInput.length > 1 ? cleanedInput[1].trim() : "";

        ReferenceArrayList<EnchantmentSelector> enchantmentSelectors = new ReferenceArrayList<>();

        if (!enchantmentsStr.equals("")) {
            var multiEnch = enchantmentsStr.split("&");

            for (String ench : multiEnch) {
                var tagSelector = TagTargetHandler.getEnchantmentSelector(ench);
                if (tagSelector != null) {
                    enchantmentSelectors.add(tagSelector);
                } else {
                    var eId = Identifier.tryParse(ench);
                    if (eId != null) {
                        enchantmentSelectors.add(new EnchantmentSelector(eId));
                    }
                }
            }
        }

        if (AutoSwitch.switchData.toolPredicates.containsKey(itemSelectorStr)) {
            itemSelector = new ItemSelector(AutoSwitch.switchData.toolPredicates.get(itemSelectorStr), itemSelectorStr);
        } else {
            var tagSelector = TagTargetHandler.getItemSelector(itemSelectorStr);
            if (tagSelector != null) {
                itemSelector = tagSelector;
            } else {
                var item = Identifier.tryParse(itemSelectorStr);
                if (item != null) {
                    itemSelector = new ItemSelector(item);
                } else {
                    itemSelector = null;
                    AutoSwitch.logger.error("Failed to generate ItemSelector of String {}", itemSelectorStr);
                }
            }
        }

        this.enchantmentSelectors = enchantmentSelectors.toArray(EnchantmentSelector[]::new);
        id = makeId();

        AutoSwitch.logger.debug("Adding item to tool map... " + input);
        AutoSwitch.switchData.toolSelectorKeys.put(input, id);
        AutoSwitch.switchData.toolSelectors.put(id, this);
    }

    @Override
    public boolean matches(ItemStack compare) {
        if (itemSelector.matches(compare.getItem())) {
            if (enchantmentsRequired()) {
                for (EnchantmentSelector selector : enchantmentSelectors) {
                    if (!selector.matches(compare)) {
                        return false;
                    }
                }
            }
            return true;
        }

        return false;
    }

    public double getRating(ItemStack stack) {
        if (!matches(stack)) return 0;
        if (enchantmentsRequired()) {
            var enchantmentRating = 0D;
            for (EnchantmentSelector selector : enchantmentSelectors) {
                enchantmentRating += selector.getRating(stack);
            }
            return enchantmentRating;
        }

        return 1;
    }

    public boolean enchantmentsRequired() {
        return enchantmentSelectors != null && enchantmentSelectors.length > 0;
    }

    public int getId() {
        return id;
    }

    public ItemSelector getItemSelector() {
        return itemSelector;
    }

    public EnchantmentSelector[] getEnchantmentSelectors() {
        return enchantmentSelectors;
    }

    @Override
    public String toString() {
        return "ToolSelector{" + configEntry() + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ToolSelector that = (ToolSelector) o;

        if (!itemSelector.equals(that.itemSelector)) return false;
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        return Arrays.equals(enchantmentSelectors, that.enchantmentSelectors);
    }

    @Override
    public int hashCode() {
        int result = itemSelector.hashCode();
        result = 31 * result + Arrays.hashCode(enchantmentSelectors);
        return result;
    }

    private int makeId() {
        if (itemSelector == null) {
            return -321574897;
        }

        // Gets hashcode for use as id. While this isn't guaranteed to be unique,
        // the chance of a collision in this use case is extremely slim. If it ever collides,
        // the following while loop should fix it. If not, revert this change to UUID or better hash.
        var id = hashCode();

        // Logic to ensure that ids are unique
        while (AutoSwitch.switchData.toolSelectorKeys.containsValue(id)) {
            id += 1;
        }

        return id;
    }

    @Override
    public String configEntry() {
        StringBuilder entry = new StringBuilder(itemSelector.configEntry());

        if (enchantmentSelectors != null && enchantmentSelectors.length > 0) {
            entry.append(itemSelector.separator());
            for (int i = 0; i < enchantmentSelectors.length; i++) {
                entry.append(enchantmentSelectors[i].configEntry());
                if (i < enchantmentSelectors.length - 1) {
                    entry.append(enchantmentSelectors[i].separator());
                }
            }
        }

        return entry.toString();
    }

    @Override
    public String separator() {
        return ", ";
    }

    @Override
    public boolean chainable() {
        return true;
    }

}
