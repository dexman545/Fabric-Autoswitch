package autoswitch.config.io;

import java.util.Locale;

import autoswitch.AutoSwitch;

import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import org.apache.commons.lang3.tuple.Pair;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

@Environment(EnvType.CLIENT)
public class ToolHandler {
    private int id = 0;

    public ToolHandler(String input) {
        // Skip the rest of the evaluation as the toolselector has already been handled
        if (AutoSwitch.switchData.toolSelectorKeys.containsKey(input)) {
            this.id = AutoSwitch.switchData.toolSelectorKeys.getInt(input);
            return;
        }
        String[] cleanedInput = input.split(";");
        String tagStr = cleanedInput[0].toLowerCase(Locale.ENGLISH).trim().replace("!", ":");
        String enchantStr =
                cleanedInput.length > 1 ? cleanedInput[1].toLowerCase(Locale.ENGLISH).trim().replace("-", ":") : "";

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

            // Gets hashcode for use as id. While this isn't guaranteed to be unique,
            // the chance of a collision in this use case is extremely slim. If it ever collides,
            // the following while loop should fix it. If not, revert this change to UUID or better hash.
            this.id = input.hashCode();

            // Logic to ensure that hashcodes
            while (AutoSwitch.switchData.toolSelectorKeys.containsValue(this.id)) {
                this.id += 1;
                AutoSwitch.logger.error("Conflicting ID generated for toolselector: {}, attempting to fix...", input);
            }

            AutoSwitch.switchData.toolSelectorKeys.put(input, this.id);

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
            AutoSwitch.switchData.toolSelectors.put(this.id, Pair.of(tagStr, enchants));
        }

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
