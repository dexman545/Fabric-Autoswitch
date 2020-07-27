package autoswitch.config.populator;

import autoswitch.AutoSwitch;
import autoswitch.config.AutoSwitchMaterialConfig;
import autoswitch.config.AutoSwitchUsableConfig;
import autoswitch.config.util.ConfigReflection;
import net.fabricmc.fabric.api.tool.attribute.v1.FabricToolTags;
import net.minecraft.block.Material;
import net.minecraft.entity.EntityGroup;
import net.minecraft.entity.EntityType;
import net.minecraft.item.*;
import org.apache.commons.lang3.tuple.Pair;

import static autoswitch.config.util.ConfigReflection.defaults;

public class ApiMapGenerator {
    public static void createApiMaps() {

        // Damage Systems
        //This is solely for testing if this system works. Do not use as it matches all items.
        //AutoSwitch.data.damageMap.put(Item.class, ItemStack::getDamage);

        // Tool Groups
        AutoSwitch.data.toolGroupings.put("pickaxe", Pair.of(FabricToolTags.PICKAXES, PickaxeItem.class));
        AutoSwitch.data.toolGroupings.put("shovel", Pair.of(FabricToolTags.SHOVELS, ShovelItem.class));
        AutoSwitch.data.toolGroupings.put("hoe", Pair.of(FabricToolTags.HOES, HoeItem.class));
        AutoSwitch.data.toolGroupings.put("shears", Pair.of(FabricToolTags.SHEARS, ShearsItem.class));
        AutoSwitch.data.toolGroupings.put("trident", Pair.of(null, TridentItem.class));
        AutoSwitch.data.toolGroupings.put("axe", Pair.of(FabricToolTags.AXES, AxeItem.class));
        AutoSwitch.data.toolGroupings.put("sword", Pair.of(FabricToolTags.SWORDS, SwordItem.class));

        // Targets
        genTargetMap();
        genConfigMaps();
    }

    // Populate maps with default values to be sent to mods
    private static void genConfigMaps() {
        ConfigReflection.defaults(AutoSwitch.data.actionConfig, AutoSwitchMaterialConfig.class);
        ConfigReflection.defaults(AutoSwitch.data.usableConfig, AutoSwitchUsableConfig.class);

        // PoC for ensuring empty values don't get passed allowing mods to override
        /*for (String key : matCfg.propertyNames()) {
            if (!matCfg.getProperty(key, "").equals("")) {
                AutoSwitch.data.actionConfig.put(key, matCfg.getProperty(key));
            }
        }

        for (String key : usableCfg.propertyNames()) {
            if (!usableCfg.getProperty(key, "").equals("")) {
                AutoSwitch.data.usableConfig.put(key, usableCfg.getProperty(key));
            }
        }*/

    }

    // Populate Targets map with default values
    private static void genTargetMap() {
        AutoSwitch.data.targets.put("solid_organic", Material.SOLID_ORGANIC);

        AutoSwitch.data.targets.put("repair_station", Material.REPAIR_STATION);

        AutoSwitch.data.targets.put("bamboo", Material.BAMBOO);

        AutoSwitch.data.targets.put("bamboo_sapling", Material.BAMBOO_SAPLING);

        AutoSwitch.data.targets.put("cactus", Material.CACTUS);

        AutoSwitch.data.targets.put("cake", Material.CAKE);

        AutoSwitch.data.targets.put("carpet", Material.CARPET);

        AutoSwitch.data.targets.put("organic_product", Material.ORGANIC_PRODUCT);

        AutoSwitch.data.targets.put("cobweb", Material.COBWEB);

        AutoSwitch.data.targets.put("soil", Material.SOIL);

        AutoSwitch.data.targets.put("egg", Material.EGG);

        AutoSwitch.data.targets.put("glass", Material.GLASS);

        AutoSwitch.data.targets.put("ice", Material.ICE);

        AutoSwitch.data.targets.put("leaves", Material.LEAVES);

        AutoSwitch.data.targets.put("metal", Material.METAL);

        AutoSwitch.data.targets.put("dense_ice", Material.DENSE_ICE);

        AutoSwitch.data.targets.put("sub_block", Material.SUPPORTED);

        AutoSwitch.data.targets.put("piston", Material.PISTON);

        AutoSwitch.data.targets.put("plant", Material.PLANT);

        AutoSwitch.data.targets.put("gourd", Material.GOURD);

        AutoSwitch.data.targets.put("redstone_lamp", Material.REDSTONE_LAMP);

        AutoSwitch.data.targets.put("replaceable_plant", Material.REPLACEABLE_PLANT);

        AutoSwitch.data.targets.put("aggregate", Material.AGGREGATE);

        AutoSwitch.data.targets.put("replaceable_underwater_plant", Material.REPLACEABLE_UNDERWATER_PLANT);

        AutoSwitch.data.targets.put("shulker_box", Material.SHULKER_BOX);

        AutoSwitch.data.targets.put("snow_layer", Material.SNOW_LAYER);

        AutoSwitch.data.targets.put("snow_block", Material.SNOW_BLOCK);

        AutoSwitch.data.targets.put("sponge", Material.SPONGE);

        AutoSwitch.data.targets.put("nether_wood", Material.NETHER_WOOD);

        AutoSwitch.data.targets.put("stone", Material.STONE);

        AutoSwitch.data.targets.put("tnt", Material.TNT);

        AutoSwitch.data.targets.put("underwater_plant", Material.UNDERWATER_PLANT);

        AutoSwitch.data.targets.put("unused_plant", Material.UNUSED_PLANT);

        AutoSwitch.data.targets.put("wood", Material.WOOD);

        AutoSwitch.data.targets.put("wool", Material.WOOL);

        AutoSwitch.data.targets.put("water", Material.WATER);

        AutoSwitch.data.targets.put("fire", Material.FIRE);

        AutoSwitch.data.targets.put("lava", Material.LAVA);

        AutoSwitch.data.targets.put("barrier", Material.BARRIER);

        AutoSwitch.data.targets.put("bubble_column", Material.BUBBLE_COLUMN);

        AutoSwitch.data.targets.put("air", Material.AIR);

        AutoSwitch.data.targets.put("portal", Material.PORTAL);

        AutoSwitch.data.targets.put("structure_void", Material.STRUCTURE_VOID);

        // Entities
        AutoSwitch.data.targets.put("aquaticentity", EntityGroup.AQUATIC);

        AutoSwitch.data.targets.put("arthropod", EntityGroup.ARTHROPOD);

        AutoSwitch.data.targets.put("defaultentity", EntityGroup.DEFAULT);

        AutoSwitch.data.targets.put("illager", EntityGroup.ILLAGER);

        AutoSwitch.data.targets.put("undead", EntityGroup.UNDEAD);

        AutoSwitch.data.targets.put("boat", EntityType.BOAT);
    }

}
