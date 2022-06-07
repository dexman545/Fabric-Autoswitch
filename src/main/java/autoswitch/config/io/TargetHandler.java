package autoswitch.config.io;

import java.util.Locale;

import autoswitch.AutoSwitch;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

/**
 * Custom type for use of parsing the materials config into something meaningful for AutoSwitch matches strings to
 * materials or entity type or group
 */
@Environment(EnvType.CLIENT)
public class TargetHandler {

    /**
     * @return returns target, may be Material, Block, EntityGroup, EntityType, or TargetGroup.
     * {@code null} if no target found.
     */
    public static Object getTarget(String str) {
        Object target = null;
        str = str.toLowerCase(Locale.ENGLISH).replace("!", ":");
        if (!AutoSwitch.switchData.targets.containsKey(str)) {
            var group = TagHandler.getTargetableTagGroup(str);
            if (group != null) {
                return group;
            }

            if (Identifier.tryParse(str) != null) {
                //todo use FutureRegistryEntry? how to handle entity/block
                // difference? make FRE have two registries to look at and pick one? what to do about conflicting
                // entries
                target = locateRegistryReference(Registry.ENTITY_TYPE, str) != null ?
                         locateRegistryReference(Registry.ENTITY_TYPE, str) :
                         locateRegistryReference(Registry.BLOCK, str);
            } else {
                AutoSwitch.logger.warn("AutoSwitch was not given a real id: " + str + " -> ignoring it");
            }
        } else {
            target = AutoSwitch.switchData.targets.get(str);
        }
        if (target == null) {
            AutoSwitch.logger
                    .warn("AutoSwitch could not find a block, entity, entity group, or material " + "by this id: " +
                          str + " -> ignoring it");
        }

        return target;
    }
    private static Object locateRegistryReference(Registry<?> registry, String str) {
        var id = Identifier.tryParse(str);
        if (registry.containsId(id)) {
            return registry.get(id);
        }

        return null;
    }

}
