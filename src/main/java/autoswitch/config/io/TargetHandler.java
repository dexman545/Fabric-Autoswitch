package autoswitch.config.io;

import java.util.Locale;

import autoswitch.AutoSwitch;
import autoswitch.selectors.futures.FutureRegistryEntry;
import autoswitch.selectors.futures.RegistryType;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.util.Identifier;

/**
 * Custom type for use of parsing the materials config into something meaningful for AutoSwitch matches strings to
 * materials or entity type or group
 */
@Environment(EnvType.CLIENT)
public class TargetHandler {

    /**
     * @return returns target, may be Material, Block, EntityGroup, EntityType, or TargetGroup. {@code null} if no
     * target found.
     */
    public static Object getTarget(String str) {
        Object target = null;
        str = str.toLowerCase(Locale.ENGLISH).replace("!", ":");
        if (!AutoSwitch.switchData.targets.containsKey(str)) {
            var group = TagHandler.getTargetableTagGroup(str);
            if (group != null) {
                return group;
            }

            Identifier id;
            if ((id = Identifier.tryParse(str)) != null) {
                target = FutureRegistryEntry.getOrCreate(RegistryType.BLOCK_OR_ENTITY, id);
            } else {
                AutoSwitch.logger.warn("AutoSwitch was not given a real id: " + str + " -> ignoring it");
            }
        } else {
            target = AutoSwitch.switchData.targets.get(str);
        }
        if (target == null) {
            AutoSwitch.logger.warn(
                    "AutoSwitch could not find a block, entity, entity group, or material " + "by this id: " + str +
                    " -> ignoring it");
        }

        return target;
    }

}
