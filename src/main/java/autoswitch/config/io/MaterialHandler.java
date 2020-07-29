package autoswitch.config.io;

import autoswitch.AutoSwitch;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

/**
 * Custom type for use of parsing the materials config into something meaningful for AutoSwitch
 * matches strings to materials or entity type or group
 */
@Environment(EnvType.CLIENT)
public class MaterialHandler {
    private final Object mat;

    public MaterialHandler(String str) {
        Object mat1 = null;
        str = str.toLowerCase().replace("-", ":");
        if (!AutoSwitch.data.targets.containsKey(str)) {
            if (Identifier.tryParse(str) != null) {
                if (Registry.ENTITY_TYPE.containsId(Identifier.tryParse(str))) {
                    mat1 = Registry.ENTITY_TYPE.get(Identifier.tryParse(str));
                }
                if (Registry.BLOCK.containsId(Identifier.tryParse(str))) {
                    mat1 = Registry.BLOCK.get(Identifier.tryParse(str));
                }
            } else {
                AutoSwitch.logger.warn("AutoSwitch could not find a material by that name: " + str + " -> ignoring it");
            }
        }

        this.mat = mat1;
    }

    /**
     * @return returns target, may be Material, Block, EntityGroup, or EntityType. Null if no material found
     */
    public Object getMat() {
        return this.mat;
    }
}
