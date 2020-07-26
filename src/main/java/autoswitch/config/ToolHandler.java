package autoswitch.config;

import autoswitch.AutoSwitch;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.Item;
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.apache.commons.lang3.tuple.Pair;

import java.util.concurrent.atomic.AtomicBoolean;

@Environment(EnvType.CLIENT)
public class ToolHandler {
    private int id = 0;

    public ToolHandler(String input) {
        String[] cleanedInput = input.split(";");
        String tagStr = cleanedInput[0].toLowerCase().trim().replace("-", ":");
        String enchantStr = cleanedInput.length > 1 ? cleanedInput[1].toLowerCase().trim().replace("-", ":") : "";

        ReferenceArrayList<Enchantment> enchants = new ReferenceArrayList<>();
        ReferenceArrayList<Identifier> enchantIdentifiers = new ReferenceArrayList<>();
        String[] multiEnch;

        if (!enchantStr.equals("")) {
            multiEnch = enchantStr.split("&");

            for (String ench : multiEnch) {
                enchantIdentifiers.add(Identifier.tryParse(ench));
            }
        }

        if (getTool(tagStr).equals("")) {
            AutoSwitch.logger.debug("Empty Tool Entry tried to parse");
        } else {

            this.id = input.hashCode();

            enchantIdentifiers.forEach(identifier -> {
                if ((!Registry.ENCHANTMENT.containsId(identifier))) {
                    if (!enchantStr.equals("")) {
                        AutoSwitch.logger.warn("An enchantment was not found in registry: " + enchantStr);
                    }
                } else {
                    enchants.add(Registry.ENCHANTMENT.get(identifier));
                }
            });

            AutoSwitch.logger.debug("Adding item to toolmap... " + input);
            AutoSwitch.data.enchantToolMap.put(id, Pair.of(tagStr, enchants));
        }

    }

    /**
     * Checks if the tool is of the correct type or not
     *
     * @param tool tool name from config
     * @param item item from hotbar
     * @return true if tool name and item match
     */
    public static boolean isCorrectType(String tool, Item item) {
        if (AutoSwitch.cfg.useNoDurablityItemsWhenUnspecified() && item.getMaxDamage() == 0) return true;
        return isCorrectTool(tool, item);

    }

    public static boolean isCorrectUseType(String tool, Item item) {
        return isCorrectTool(tool, item);
    }

    private static boolean isCorrectTool(String tool, Item item) {
        AtomicBoolean matches = new AtomicBoolean(false);

        AutoSwitch.data.toolGroupings.forEach((toolKey, tagClassPair) -> {
            if (tool.equals(toolKey) || tool.equals("any")) {
                if (checkTagAndClass(tagClassPair.getLeft(), tagClassPair.getRight(), item)) {
                    matches.set(true);
                }
            }
        });

        return matches.get() || (Registry.ITEM.getId(item).equals(Identifier.tryParse(tool)));
    }

    private static boolean checkTagAndClass(Tag<Item> tag, Class<?> clazz, Item item) {
        boolean tagCheck = false;
        boolean classCheck = false;

        if (tag != null) {
            tagCheck = tag.contains(item);
        }

        if (clazz != null) {
            classCheck = clazz.isInstance(item);
        }

        return tagCheck || classCheck;
    }

    private String getTool(String t) {
        switch (t) {
            case "axe":
            case "trident":
            case "shovel":
            case "pickaxe":
            case "sword":
            case "shears":
            case "hoe":
            case "any":
            default:
                return Identifier.tryParse(t) != null ? t : "";
        }

    }

    public int getId() {
        return id;
    }
}
