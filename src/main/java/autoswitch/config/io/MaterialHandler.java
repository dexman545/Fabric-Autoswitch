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
public class MaterialHandler {
    private final Object mat;

    public MaterialHandler(String str) {
        Object mat1 = null;
        str = str.toLowerCase(Locale.ENGLISH).replace("!", ":");
        if (!AutoSwitch.switchData.targets.containsKey(str)) {
            var group = TagHandler.getTargetableTagGroup(str);
            if (group != null) {
                this.mat = group;
                return;
            }

            if (Identifier.tryParse(str) != null) {
                mat1 = locateMat(Registry.ENTITY_TYPE, str) != null ? locateMat(Registry.ENTITY_TYPE, str)
                                                                    : locateMat(Registry.BLOCK, str);
            } else {
                AutoSwitch.logger.warn("AutoSwitch was not given a real id: " + str + " -> ignoring it");
            }
        } else {
            mat1 = AutoSwitch.switchData.targets.get(str);
        }
        if (mat1 == null) {
            AutoSwitch.logger
                    .warn("AutoSwitch could not find a block, entity, entity group, or material " + "by this id: " +
                          str + " -> ignoring it");
        }
        this.mat = mat1;
    }

    private Object locateMat(Registry<?> registry, String str) {
        if (registry.containsId(Identifier.tryParse(str))) {
            return registry.get(Identifier.tryParse(str));
        }

        return null;
    }

    /**
     * @return returns target, may be Material, Block, EntityGroup, or EntityType. Null if no material found
     */
    public Object getMat() {
        return this.mat;
    }

}
