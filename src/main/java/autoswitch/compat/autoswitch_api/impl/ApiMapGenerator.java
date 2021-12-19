package autoswitch.compat.autoswitch_api.impl;

import autoswitch.AutoSwitch;
import autoswitch.config.AutoSwitchAttackActionConfig;
import autoswitch.config.AutoSwitchUseActionConfig;
import autoswitch.config.util.ConfigReflection;
import autoswitch.targetable.custom.TargetableGroup;
import autoswitch.util.SwitchData;
import autoswitch.util.SwitchUtil;

import net.fabricmc.fabric.api.tool.attribute.v1.FabricToolTags;
import net.fabricmc.loader.api.SemanticVersion;
import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.api.VersionParsingException;

import net.minecraft.block.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityGroup;
import net.minecraft.entity.EntityType;
import net.minecraft.item.*;

import org.apache.commons.lang3.tuple.Pair;

public class ApiMapGenerator {
    public static void createApiMaps() {

        // Damage Systems
        //This is solely for testing if this system works. Do not use as it matches all items.
        //AutoSwitch.data.damageMap.put(Item.class, ItemStack::getDamage);

        // Tool Groups
        AutoSwitch.switchData.toolGroupings.put("pickaxe", Pair.of(FabricToolTags.PICKAXES, PickaxeItem.class));
        AutoSwitch.switchData.toolGroupings.put("shovel", Pair.of(FabricToolTags.SHOVELS, ShovelItem.class));
        AutoSwitch.switchData.toolGroupings.put("hoe", Pair.of(FabricToolTags.HOES, HoeItem.class));
        AutoSwitch.switchData.toolGroupings.put("shears", Pair.of(FabricToolTags.SHEARS, ShearsItem.class));
        AutoSwitch.switchData.toolGroupings.put("trident", Pair.of(null, TridentItem.class));
        AutoSwitch.switchData.toolGroupings.put("axe", Pair.of(FabricToolTags.AXES, AxeItem.class));
        AutoSwitch.switchData.toolGroupings.put("sword", Pair.of(FabricToolTags.SWORDS, SwordItem.class));

        // Targets
        genTargetMap();
        genConfigMaps();
    }

    // Populate Targets map with default values
    private static void genTargetMap() {
        addTarget("solid_organic", Material.SOLID_ORGANIC);

        addTarget("repair_station", Material.REPAIR_STATION);

        addTarget("bamboo", Material.BAMBOO);

        addTarget("bamboo_sapling", Material.BAMBOO_SAPLING);

        addTarget("cactus", Material.CACTUS);

        addTarget("cake", Material.CAKE);

        addTarget("carpet", Material.CARPET);

        addTarget("organic_product", Material.ORGANIC_PRODUCT);

        addTarget("cobweb", Material.COBWEB);

        addTarget("soil", Material.SOIL);

        addTarget("egg", Material.EGG);

        addTarget("glass", Material.GLASS);

        addTarget("ice", Material.ICE);

        addTarget("leaves", Material.LEAVES);

        addTarget("metal", Material.METAL);

        addTarget("dense_ice", Material.DENSE_ICE);

        addTarget("sub_block", Material.DECORATION);

        addTarget("piston", Material.PISTON);

        addTarget("plant", Material.PLANT);

        addTarget("gourd", Material.GOURD);

        addTarget("redstone_lamp", Material.REDSTONE_LAMP);

        addTarget("replaceable_plant", Material.REPLACEABLE_PLANT);

        addTarget("aggregate", Material.AGGREGATE);

        addTarget("replaceable_underwater_plant", Material.REPLACEABLE_UNDERWATER_PLANT);

        addTarget("shulker_box", Material.SHULKER_BOX);

        addTarget("snow_layer", Material.SNOW_LAYER);

        addTarget("snow_block", Material.SNOW_BLOCK);

        addTarget("sponge", Material.SPONGE);

        addTarget("nether_wood", Material.NETHER_WOOD);

        addTarget("stone", Material.STONE);

        addTarget("tnt", Material.TNT);

        addTarget("nether_shoots", Material.NETHER_SHOOTS);

        addTarget("underwater_plant", Material.UNDERWATER_PLANT);

        addTarget("moss_block", Material.MOSS_BLOCK);

        addTarget("wood", Material.WOOD);

        addTarget("wool", Material.WOOL);

        addTarget("water", Material.WATER);

        addTarget("fire", Material.FIRE);

        addTarget("lava", Material.LAVA);

        addTarget("barrier", Material.BARRIER);

        addTarget("bubble_column", Material.BUBBLE_COLUMN);

        addTarget("air", Material.AIR);

        addTarget("portal", Material.PORTAL);

        addTarget("structure_void", Material.STRUCTURE_VOID);

        if (isAcceptableVersion("1.17-alpha.20.49.a")) {
            addTarget("amethyst", Material.AMETHYST);

            addTarget("passable_snow_block", Material.POWDER_SNOW);

            addTarget("sculk", Material.SCULK);
        }

        // Entities
        addTarget("aquaticEntity", EntityGroup.AQUATIC);

        addTarget("arthropod", EntityGroup.ARTHROPOD);

        addTarget("defaultEntity", EntityGroup.DEFAULT);

        addTarget("illager", EntityGroup.ILLAGER);

        addTarget("undead", EntityGroup.UNDEAD);

        addTarget("ender_dragon", EntityType.ENDER_DRAGON);

        addTarget("minecart", new TargetableGroup<EntityType<? extends Entity>>(EntityType.MINECART,
                                                                                EntityType.CHEST_MINECART,
                                                                                EntityType.HOPPER_MINECART,
                                                                                EntityType.TNT_MINECART,
                                                                                EntityType.FURNACE_MINECART,
                                                                                EntityType.COMMAND_BLOCK_MINECART,
                                                                                EntityType.SPAWNER_MINECART));

        // Item Use
        addTarget("bow_action", SwitchData.itemTarget);
    }

    // Populate maps with default values to be sent to mods
    private static void genConfigMaps() {
        ConfigReflection.defaults(AutoSwitch.switchData.attackConfig, AutoSwitchAttackActionConfig.class);
        ConfigReflection.defaults(AutoSwitch.switchData.usableConfig, AutoSwitchUseActionConfig.class);

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

    private static void addTarget(String name, Object target) {
        try {
            AutoSwitch.switchData.targets.put(name, target);
        } catch (NoSuchFieldError e) {
            AutoSwitch.logger.debug("Failed to add target - Name: {}, ID: {}", name, e.getMessage());
        }
    }

    private static boolean isAcceptableVersion(String minVersion) {
        try {
            return SemanticVersion.parse(SwitchUtil.getMinecraftVersion())
                                  .compareTo((Version) SemanticVersion.parse(minVersion)) >= 0;
        } catch (VersionParsingException e) {
            AutoSwitch.logger.error(e);
        }

        return false;
    }

}
