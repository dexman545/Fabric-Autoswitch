package autoswitch;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Material;
import net.minecraft.entity.EntityGroup;
import net.minecraft.entity.EntityType;
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
        switch (str.toLowerCase()){
            case "solid_organic":
                this.mat = Material.ORGANIC;
                break;
            case "repair_station":
                this.mat = Material.ANVIL;
                break;
            case "bamboo":
                this.mat = Material.BAMBOO;
                break;
            case "bamboo_sapling":
                this.mat = Material.BAMBOO_SAPLING;
                break;
            case "cactus":
                this.mat = Material.CACTUS;
                break;
            case "cake":
                this.mat = Material.CAKE;
                break;
            case "carpet":
                this.mat = Material.CARPET;
                break;
            case "organic_product":
                this.mat = Material.CLAY;
                break;
            case "cobweb":
                this.mat = Material.COBWEB;
                break;
            case "soil":
                this.mat = Material.EARTH;
                break;
            case "egg":
                this.mat = Material.EGG;
                break;
            case "glass":
                this.mat = Material.GLASS;
                break;
            case "ice":
                this.mat = Material.ICE;
                break;
            case "leaves":
                this.mat = Material.LEAVES;
                break;
            case "metal":
                this.mat = Material.METAL;
                break;
            case "dense_ice":
                this.mat = Material.PACKED_ICE;
                break;
            case "supported":
                this.mat = Material.PART;
                break;
            case "piston":
                this.mat = Material.PISTON;
                break;
            case "plant":
                this.mat = Material.PLANT;
                break;
            case "gourd":
                this.mat = Material.PUMPKIN;
                break;
            case "redstone_lamp":
                this.mat = Material.REDSTONE_LAMP;
                break;
            case "replaceable_plant":
                this.mat = Material.REPLACEABLE_PLANT;
                break;
            case "aggregate":
                this.mat = Material.SAND;
                break;
            case "replaceable_underwater_plant":
                this.mat = Material.SEAGRASS;
                break;
            case "shulker_box":
                this.mat = Material.SHULKER_BOX;
                break;
            case "snow_layer":
                this.mat = Material.SNOW;
                break;
            case "snow_block":
                this.mat = Material.SNOW_BLOCK;
                break;
            case "sponge":
                this.mat = Material.SPONGE;
                break;
            case "nether_wood":
                this.mat = Material.NETHER_WOOD;
                break;
            case "stone":
                this.mat = Material.STONE;
                break;
            case "tnt":
                this.mat = Material.TNT;
                break;
            case "underwater_plant":
                this.mat = Material.UNDERWATER_PLANT;
                break;
            case "unused_plant":
                this.mat = Material.UNUSED_PLANT;
                break;
            case "wood":
                this.mat = Material.WOOD;
                break;
            case "wool":
                this.mat = Material.WOOL;
                break;
            case "water":
                this.mat = Material.WATER;
                break;
            case "fire":
                this.mat = Material.FIRE;
                break;
            case "lava":
                this.mat = Material.LAVA;
                break;
            case "barrier":
                this.mat = Material.BARRIER;
                break;
            case "bubble_column":
                this.mat = Material.BUBBLE_COLUMN;
                break;
            case "air":
                this.mat = Material.AIR;
                break;
            case "portal":
                this.mat = Material.PORTAL;
                break;
            case "structure_void":
                this.mat = Material.STRUCTURE_VOID;
                break;
            //entities
            case "aquaticentity":
                this.mat = EntityGroup.AQUATIC;
                break;
            case "arthropod":
                this.mat = EntityGroup.ARTHROPOD;
                break;
            case "defaultentity":
                this.mat = EntityGroup.DEFAULT;
                break;
            case "illager":
                this.mat = EntityGroup.ILLAGER;
                break;
            case "undead":
                this.mat = EntityGroup.UNDEAD;
                break;
            case "boat":
                this.mat = EntityType.BOAT;
                break;

            default:
                if (Identifier.tryParse(str.toLowerCase()) != null) { //handle use event
                    if (Registry.ENTITY_TYPE.containsId(Identifier.tryParse(str.toLowerCase()))) {
                        this.mat = Registry.ENTITY_TYPE.get(Identifier.tryParse(str.toLowerCase()));
                        break;
                    }
                    if (Registry.BLOCK.containsId(Identifier.tryParse(str.toLowerCase()))) {
                        this.mat = Registry.BLOCK.get(Identifier.tryParse(str.toLowerCase()));
                        break;
                    }
                }

                this.mat = null;
                AutoSwitch.logger.warn("AutoSwitch could not find a material by that name: " + str + " -> ignoring it");
        }

    }

    /**
     * @return returns target, may be Material, EntityGroup, or EntityType. Null if no material found
     */
    public Object getMat(){
        return this.mat;
    }
}
