package autoswitch.compat.autoswitch_api.impl;

import java.util.function.Predicate;

import autoswitch.AutoSwitch;
import autoswitch.actions.Action;
import autoswitch.config.AutoSwitchAttackActionConfig;
import autoswitch.config.AutoSwitchEventActionConfig;
import autoswitch.config.AutoSwitchUseActionConfig;
import autoswitch.config.util.ConfigReflection;
import autoswitch.selectors.ItemTarget;
import autoswitch.selectors.TargetableGroup;
import autoswitch.selectors.TargetableGroup.TargetPredicate;
import autoswitch.util.SwitchUtil;

import org.jetbrains.annotations.NotNull;

import net.fabricmc.fabric.api.tag.client.v1.ClientTags;

import net.minecraft.block.Block;
import net.minecraft.entity.Bucketable;
import net.minecraft.entity.EntityGroup;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.HoeItem;
import net.minecraft.item.Item;
import net.minecraft.item.PickaxeItem;
import net.minecraft.item.RangedWeaponItem;
import net.minecraft.item.ShearsItem;
import net.minecraft.item.ShovelItem;
import net.minecraft.item.SwordItem;
import net.minecraft.item.TridentItem;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

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
        assert anyTool != null;
        anyTool = anyTool.or(o -> { // Added in 1.19.4
            if (o instanceof Item item) {
                return ClientTags.isInWithLocalFallback(
                        TagKey.of(RegistryKeys.ITEM, new Identifier("minecraft", "tools")),
                        item
                );
            }
            return false;
        });
        AutoSwitch.switchData.toolPredicates.put("any", anyTool);

        // Don't include bow group in any group
        AutoSwitch.switchData.toolPredicates.computeIfAbsent("bow", s -> makeToolPredicate(s, RangedWeaponItem.class));

        // Targets
        genTargetMap();
        genConfigMaps();

        TargetableGroup.validatePredicates();
    }

    private static Predicate<Object> makeToolPredicate(String toolName, @NotNull Class<? extends Item> itemClass) {
        var pluralName = toolName.endsWith("s") ? toolName : toolName + "s";
        var fabricTag = TagKey.of(RegistryKeys.ITEM, new Identifier("fabric", pluralName));


        TagKey<Item> commonTag;
        TagKey<Item> mcTag;
        if (toolName.equals("trident")) {
            commonTag = TagKey.of(RegistryKeys.ITEM, new Identifier("c", "spears"));
            // Other tags added in 1.19.4
            mcTag = TagKey.of(RegistryKeys.ITEM, new Identifier("minecraft", "spears"));
        } else {
            commonTag = TagKey.of(RegistryKeys.ITEM, new Identifier("c", pluralName));
            mcTag = TagKey.of(RegistryKeys.ITEM, new Identifier("minecraft", pluralName));// Added in 1.19.4
        }

        // todo move to using itemstack rather than item itself - api change?
        return o -> {
            if (o instanceof Item item) {
                return itemClass.isInstance(item) || ClientTags.isInWithLocalFallback(fabricTag, item) ||
                       ClientTags.isInWithLocalFallback(commonTag, item) ||
                       ClientTags.isInWithLocalFallback(mcTag, item);
            }
            return false;
        };
    }

    // Populate Targets map with default values
    private static void genTargetMap() {
        // Blocks
        addDefaultTarget(TagKey.of(RegistryKeys.BLOCK, new Identifier("autoswitch:shears_efficient")));
        addDefaultTarget(TagKey.of(RegistryKeys.BLOCK, new Identifier("autoswitch:sword_efficient")));
        addDefaultTarget(BlockTags.HOE_MINEABLE);//todo wrap in AS tags?
        addDefaultTarget(BlockTags.AXE_MINEABLE);
        addDefaultTarget(BlockTags.PICKAXE_MINEABLE);
        addDefaultTarget(BlockTags.SHOVEL_MINEABLE);
        //todo leave as tag target, or give special names?

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
        addTarget("bow_action", ItemTarget.INSTANCE);
    }

    // Populate maps with default values to be sent to mods
    private static void genConfigMaps() {
        ConfigReflection.defaults(Action.ATTACK.getConfigMap(), AutoSwitchAttackActionConfig.class);
        ConfigReflection.defaults(Action.INTERACT.getConfigMap(), AutoSwitchUseActionConfig.class);
        ConfigReflection.defaults(Action.EVENT.getConfigMap(), AutoSwitchEventActionConfig.class);

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

    private static void addDefaultTarget(TagKey<Block> tag) {
        Predicate<Object> predicate = o -> {
            if (o instanceof Block b) {
                return ClientTags.isInWithLocalFallback(tag, b);
            }
            return false;
        };

        AutoSwitch.switchData.defaultTargets.add(predicate);

        var name = "block@" + tag.id().toString().replaceAll(":", "!");
        addTarget(name, predicate);
    }

}
