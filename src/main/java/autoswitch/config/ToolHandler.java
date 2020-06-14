package autoswitch.config;

import autoswitch.AutoSwitch;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.tool.attribute.v1.FabricToolTags;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.*;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.apache.commons.lang3.tuple.Pair;

import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

@Environment(EnvType.CLIENT)
public class ToolHandler {
    private UUID id = null;

    public ToolHandler(String input) {
        String[] cleanedInput = input.split(";");
        String tagStr = cleanedInput[0].toLowerCase().trim().replace("-", ":");
        String enchantStr = cleanedInput.length > 1 ? cleanedInput[1].toLowerCase().trim().replace("-", ":") : "";

        CopyOnWriteArrayList<Enchantment> enchants = new CopyOnWriteArrayList<>();
        CopyOnWriteArrayList<Identifier> enchantIdentifiers = new CopyOnWriteArrayList<>();
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
            this.id = UUID.nameUUIDFromBytes(input.getBytes());

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
        if ((tool.equals("pickaxe") || tool.equals("any")) && (FabricToolTags.PICKAXES.contains(item) || item instanceof PickaxeItem)) {
            return true;
        } else if ((tool.equals("shovel") || tool.equals("any")) && (FabricToolTags.SHOVELS.contains(item) || item instanceof ShovelItem)) {
            return true;
        } else if ((tool.equals("hoe") || tool.equals("any")) && (FabricToolTags.HOES.contains(item) || item instanceof HoeItem)) {
            return true;
        } else if ((tool.equals("shears") || tool.equals("any")) && (FabricToolTags.SHEARS.contains(item) || item instanceof ShearsItem)) {
            return true;
        } else if ((tool.equals("trident") || tool.equals("any")) && (item instanceof TridentItem)) {
            return true;
        } else if ((tool.equals("axe") || tool.equals("any")) && (FabricToolTags.AXES.contains(item) || item instanceof AxeItem)) {
            return true;
        } else if ((tool.equals("sword") || tool.equals("any")) && (FabricToolTags.SWORDS.contains(item) || item instanceof SwordItem)) {
            return true;
        } else return (Registry.ITEM.getId(item).equals(Identifier.tryParse(tool)));
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

    public UUID getId() {
        return id;
    }
}
