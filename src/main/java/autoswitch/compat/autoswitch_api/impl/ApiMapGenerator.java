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

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Bucketable;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.ShearsItem;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.level.block.Block;

public class ApiMapGenerator {
    public static void createApiMaps() {

        // Damage Systems
        //This is solely for testing if this system works. Do not use as it matches all items.
        //AutoSwitch.data.damageMap.put(Item.class, ItemStack::getDamage);

        // Tool Groups via Predicate
        AutoSwitch.switchData.toolPredicates.computeIfAbsent("pickaxe", s -> makeToolPredicate(s, null));
        AutoSwitch.switchData.toolPredicates.computeIfAbsent("shovel", s -> makeToolPredicate(s, ShovelItem.class));
        AutoSwitch.switchData.toolPredicates.computeIfAbsent("hoe", s -> makeToolPredicate(s, HoeItem.class));
        AutoSwitch.switchData.toolPredicates.computeIfAbsent("shears", s -> makeToolPredicate(s, ShearsItem.class));
        AutoSwitch.switchData.toolPredicates.computeIfAbsent("trident", s -> makeToolPredicate(s, TridentItem.class));
        AutoSwitch.switchData.toolPredicates.computeIfAbsent("axe", s -> makeToolPredicate(s, AxeItem.class));
        AutoSwitch.switchData.toolPredicates.computeIfAbsent("sword", s -> makeToolPredicate(s, null));

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
                        TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("minecraft", "tools")),
                        item
                );
            }
            return false;
        });
        AutoSwitch.switchData.toolPredicates.put("any", anyTool);

        // Don't include bow group in any group
        AutoSwitch.switchData.toolPredicates.computeIfAbsent("bow", s -> makeToolPredicate(s, ProjectileWeaponItem.class));

        // Targets
        genTargetMap();
        genConfigMaps();

        TargetableGroup.validatePredicates();
    }

    private static Predicate<Object> makeToolPredicate(String toolName, Class<? extends Item> itemClass) {
        var pluralName = toolName.endsWith("s") ? toolName : toolName + "s";
        var fabricTag = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("fabric", pluralName));


        TagKey<Item> commonTag;
        TagKey<Item> mcTag;
        if (toolName.equals("trident")) {
            commonTag = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", "tools/spears"));
            // Other tags added in 1.19.4
            mcTag = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("minecraft", "spears"));
        } else {
            commonTag = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", "tools/"+pluralName));
            mcTag = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("minecraft", pluralName));// Added in 1.19.4
        }

        // todo move to using itemstack rather than item itself - api change?
        return o -> {
            if (o instanceof Item item) {
                return (itemClass != null && itemClass.isInstance(item)) ||
                       ClientTags.isInWithLocalFallback(fabricTag, item) ||
                       ClientTags.isInWithLocalFallback(commonTag, item) ||
                       ClientTags.isInWithLocalFallback(mcTag, item);
            }
            return false;
        };
    }

    // Populate Targets map with default values
    private static void genTargetMap() {
        // Blocks
        //todo wrap in AS tags?
        //todo order dependent
        addDefaultBlockTarget(TagKey.create(Registries.BLOCK, ResourceLocation.parse("autoswitch:shears_efficient")));
        addDefaultBlockTarget(BlockTags.MINEABLE_WITH_HOE);
        addDefaultBlockTarget(BlockTags.MINEABLE_WITH_AXE);
        addDefaultBlockTarget(BlockTags.MINEABLE_WITH_PICKAXE);
        addDefaultBlockTarget(BlockTags.MINEABLE_WITH_SHOVEL);
        addDefaultBlockTarget(TagKey.create(Registries.BLOCK, ResourceLocation.parse("autoswitch:sword_efficient")));
        //todo leave as tag target, or give special names?

        // Entities
        // See SENSITIVE_TO_IMPALING tag, not references in damage source for the tag
        addDefaultEntityTarget(EntityTypeTags.AQUATIC);

        addDefaultEntityTarget("defaultEntity", e -> e instanceof LivingEntity);

        // See SENSITIVE_TO_BANE_OF_ARTHROPODS tag, not references in damage source for the tag
        addDefaultEntityTarget(EntityTypeTags.ARTHROPOD);

        addDefaultEntityTarget(EntityTypeTags.ILLAGER);

        addDefaultEntityTarget(EntityTypeTags.UNDEAD);

        addTarget("ender_dragon", EntityType.ENDER_DRAGON);

        addTarget(new TargetableGroup<>("minecart",
                                        new TargetPredicate("minecarts",
                                                            e -> e instanceof AbstractMinecart)));

        addTarget(new TargetableGroup<>("bucketable_swimmer",
                                new TargetPredicate("things that extend the Bucketable interface",
                                                    e -> e instanceof Bucketable)));

        if (SwitchUtil.isAcceptableVersion("1.19-alpha.22.19.a")) {
            addTarget(new TargetableGroup<>("boats",
                                            new TargetPredicate("boats",
                                                                e -> e instanceof Boat)));
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

    private static void addDefaultBlockTarget(TagKey<Block> tag) {
        Predicate<Object> predicate = o -> {
            if (o instanceof Block b) {
                return ClientTags.isInWithLocalFallback(tag, b);
            }
            return false;
        };

        AutoSwitch.switchData.defaultTargets.add(predicate);

        var name = "block@" + tag.location().toString().replaceAll(":", "!");
        addTarget(name, predicate);
    }

    private static void addDefaultEntityTarget(TagKey<EntityType<?>> tag) {
        Predicate<Object> predicate = o -> {
            if (o instanceof Entity e) {
                return ClientTags.isInWithLocalFallback(tag, e.getType());
            }
            return false;
        };

        AutoSwitch.switchData.defaultTargets.add(predicate);

        var name = "entity@" + tag.location().toString().replaceAll(":", "!");
        addTarget(name, predicate);
    }

    private static void addDefaultEntityTarget(String name, Predicate<Entity> predicate) {
        Predicate<Object> p = o -> {
            if (o instanceof Entity e) {
                return predicate.test(e);
            }
            return false;
        };
        AutoSwitch.switchData.defaultTargets.add(p);
        addTarget(name, p);
    }

}
