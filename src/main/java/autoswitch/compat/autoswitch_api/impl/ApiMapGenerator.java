package autoswitch.compat.autoswitch_api.impl;

import java.util.function.Predicate;

import autoswitch.AutoSwitch;
import autoswitch.config.AutoSwitchAttackActionConfig;
import autoswitch.config.AutoSwitchUseActionConfig;
import autoswitch.config.util.ConfigReflection;
import autoswitch.selectors.TargetableGroup;
import autoswitch.selectors.TargetableGroup.TargetPredicate;
import autoswitch.util.SwitchData;
import autoswitch.util.SwitchUtil;

import org.jetbrains.annotations.NotNull;

import net.fabricmc.fabric.api.tag.client.v1.ClientTags;

import net.minecraft.block.Material;
import net.minecraft.entity.Bucketable;
import net.minecraft.entity.EntityGroup;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.HoeItem;
import net.minecraft.item.Item;
import net.minecraft.item.PickaxeItem;
import net.minecraft.item.ShearsItem;
import net.minecraft.item.ShovelItem;
import net.minecraft.item.SwordItem;
import net.minecraft.item.TridentItem;
import net.minecraft.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class ApiMapGenerator {
    public static void createApiMaps() {

        // Damage Systems
        //This is solely for testing if this system works. Do not use as it matches all items.
        //AutoSwitch.data.damageMap.put(Item.class, ItemStack::getDamage);

        // Tool Groups
        /*AutoSwitch.switchData.toolGroupings.put("pickaxe", Pair.of(null, PickaxeItem.class));
        AutoSwitch.switchData.toolGroupings.put("shovel", Pair.of(null, ShovelItem.class));
        AutoSwitch.switchData.toolGroupings.put("hoe", Pair.of(null, HoeItem.class));
        AutoSwitch.switchData.toolGroupings.put("shears", Pair.of(null, ShearsItem.class));
        AutoSwitch.switchData.toolGroupings.put("trident", Pair.of(null, TridentItem.class));
        AutoSwitch.switchData.toolGroupings.put("axe", Pair.of(null, AxeItem.class));
        AutoSwitch.switchData.toolGroupings.put("sword", Pair.of(null, SwordItem.class));*/

        // Tool Groups via Predicate
        AutoSwitch.switchData.toolPredicates.computeIfAbsent("pickaxe", s -> makeToolPredicate(s, PickaxeItem.class));
        AutoSwitch.switchData.toolPredicates.computeIfAbsent("shovel", s -> makeToolPredicate(s, ShovelItem.class));
        AutoSwitch.switchData.toolPredicates.computeIfAbsent("hoe", s -> makeToolPredicate(s, HoeItem.class));
        AutoSwitch.switchData.toolPredicates.computeIfAbsent("shears", s -> makeToolPredicate(s, ShearsItem.class));
        AutoSwitch.switchData.toolPredicates.computeIfAbsent("trident", s -> makeToolPredicate(s, TridentItem.class));
        AutoSwitch.switchData.toolPredicates.computeIfAbsent("axe", s -> makeToolPredicate(s, AxeItem.class));
        AutoSwitch.switchData.toolPredicates.computeIfAbsent("sword", s -> makeToolPredicate(s, SwordItem.class));

        // Any tool
        Predicate<Object> anyTool = null;
        for (Predicate<Object> tp : AutoSwitch.switchData.toolPredicates.values()) {
            if (anyTool == null) {
                anyTool = tp;
                continue;
            }
            anyTool = anyTool.or(tp);
        }
        AutoSwitch.switchData.toolPredicates.put("any", anyTool);

        // Targets
        genTargetMap();
        genConfigMaps();

        TargetableGroup.validatePredicates();
    }

    private static Predicate<Object> makeToolPredicate(String toolName, @NotNull Class<? extends Item> itemClass) {
        var pluralName = toolName.endsWith("s") ? toolName : toolName + "s";
        var fabricTag = TagKey.of(Registry.ITEM_KEY, new Identifier("fabric", pluralName));
        var commonTag = TagKey.of(Registry.ITEM_KEY, new Identifier("c", pluralName));

        // todo move to using itemstack rather than item itself - api change?
        return o -> {
            if (o instanceof Item item) {
                return itemClass.isInstance(item) || ClientTags.isInWithLocalFallback(fabricTag, item) ||
                       ClientTags.isInWithLocalFallback(commonTag, item);
            }
            return false;
        };
    }

    // Populate Targets map with default values
    private static void genTargetMap() {
        // Blocks
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

        addTarget("stone", Material.STONE);

        addTarget("tnt", Material.TNT);

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

        if (SwitchUtil.isAcceptableVersion("1.16-alpha.20.6.a")) {
            addTarget("nether_wood", Material.NETHER_WOOD);
        }

        if (SwitchUtil.isAcceptableVersion("1.16.2-beta.2")) {
            addTarget("nether_shoots", Material.NETHER_SHOOTS);
        }

        if (SwitchUtil.isAcceptableVersion("1.17-alpha.20.45.a")) {
            addTarget("amethyst", Material.AMETHYST);
        }

        if (SwitchUtil.isAcceptableVersion("1.17-alpha.20.46.a")) {
            addTarget("passable_snow_block", Material.POWDER_SNOW);
        }

        if (SwitchUtil.isAcceptableVersion("1.17-alpha.20.49.a")) {
            addTarget("sculk", Material.SCULK);
        }

        if (SwitchUtil.isAcceptableVersion("1.19-alpha.22.19.a")) {
            addTarget("froglight", Material.FROGLIGHT);
            addTarget("frogspawn", Material.FROGSPAWN);
        }

        // Entities
        addTarget("aquaticEntity", EntityGroup.AQUATIC);

        addTarget("arthropod", EntityGroup.ARTHROPOD);

        addTarget("defaultEntity", EntityGroup.DEFAULT);

        addTarget("illager", EntityGroup.ILLAGER);

        addTarget("undead", EntityGroup.UNDEAD);

        addTarget("ender_dragon", EntityType.ENDER_DRAGON);

        addTarget(new TargetableGroup<>("minecart",
                                        new TargetPredicate("minecarts",
                                                            e -> e instanceof AbstractMinecartEntity)));

        addTarget(new TargetableGroup<>("bucketable_swimmer",
                                new TargetPredicate("things that extend the Bucketable interface",
                                                    e -> e instanceof Bucketable)));

        if (SwitchUtil.isAcceptableVersion("1.19-alpha.22.19.a")) {
            addTarget(new TargetableGroup<>("boats",
                                            new TargetPredicate("boats",
                                                                e -> e instanceof BoatEntity)));
        }

        // Item Use
        addTarget("bow_action", SwitchData.itemTarget);
    }

    // Populate maps with default values to be sent to mods
    private static void genConfigMaps() {
        ConfigReflection.defaults(AutoSwitch.switchData.attackConfig, AutoSwitchAttackActionConfig.class);
        ConfigReflection.defaults(AutoSwitch.switchData.usableConfig, AutoSwitchUseActionConfig.class);

        // PoC for ensuring empty values don't get passed to mods via the API, allowing mods to override
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

    private static void addTarget(TargetableGroup<?> target) {
        addTarget(target.getGroupName(), target);
    }

    private static void addTarget(String name, Object target) {
        try {
            AutoSwitch.switchData.targets.put(name, target);
        } catch (NoSuchFieldError e) {
            AutoSwitch.logger.debug("Failed to add target - Name: {}, ID: {}", name, e.getMessage());
        }
    }

}
