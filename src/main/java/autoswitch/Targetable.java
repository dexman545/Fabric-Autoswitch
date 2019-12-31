package autoswitch;

import net.fabricmc.fabric.api.tools.FabricToolTags;
import net.minecraft.block.BlockState;
import net.minecraft.block.Material;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityGroup;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.item.*;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

/**
 * Parent class for Targetable type. Used to establish shared functions and parameters
 * that are used for manipulating the player's selected slot
 *
 */
@SuppressWarnings("WeakerAccess")
abstract class Targetable {
    HashMap<String, ArrayList<Object>> toolTargetLists;
    LinkedHashMap<String, ArrayList<Integer>> toolLists;
    PlayerEntity player;
    AutoSwitchConfig cfg;
    Boolean onMP;


    /**
     * Base constructor for Targetable, initializes the class parameters and
     * fetches the target map and initial tool map based on configs passed to ti
     *
     * @param player player this will effect
     * @param cfg cfg controlling AutoSwitch functionality
     * @param matCfg material config controlling what tools target
     * @param onMP whether the player is on a remote server. If given nul, will assume that AutoSwitch is allowed
     */
    public Targetable(PlayerEntity player, AutoSwitchConfig cfg, AutoSwitchMaterialConfig matCfg, Boolean onMP) {
        toolTargetLists = new AutoSwitchLists(cfg, matCfg).getToolTargetLists();
        toolLists = new AutoSwitchLists(cfg, matCfg).getToolLists();
        this.cfg = cfg;
        this.onMP = (onMP != null ? onMP : false);
        this.player = player;
    }


    /**
     * the of methods send the target to the correct function to handle it
     * @return returns the correct Targetable subclass to handle the operation
     */
    static Targetable of(Entity target, PlayerEntity player, Boolean onMP, AutoSwitchConfig cfg, AutoSwitchMaterialConfig matCfg) {
        return new TargetableEntity(target, player, cfg, matCfg, onMP);
    }

    static Targetable of(BlockState target, PlayerEntity player, Boolean onMP, AutoSwitchConfig cfg, AutoSwitchMaterialConfig matCfg) {
        return new TargetableMaterial(target, player, cfg, matCfg, onMP);
    }

    static Targetable of(int prevSlot, PlayerEntity player) {
        return new TargetableNone(prevSlot, player);
    }


    /**
     * Pulls the list of itemstacks from the player's hotbar and send the stack and slot number
     * to populate the tool map. Sends an air item if th slow is empty.
     * @param player player whose inventory will be checked
     */
    public void populateToolLists(PlayerEntity player) {
        List<ItemStack> hotbar = player.inventory.main.subList(0, 9);
        for (int i=0; i<9; i++) {
            populateCommonToolList(hotbar.get(i), i);
            populateTargetTools(hotbar.get(i), i);

        }

    }


    /**
     * Populates the tool map for tools shared between entities and blocks.
     * Also populates fortAxes and silkAxes here as to avoid repeating the isAxe check elsewhere
     * @param stack itemstack
     * @param i slot the itemstack was in
     */
    private void populateCommonToolList(ItemStack stack, int i) {
        Item item = stack.getItem();
        if (FabricToolTags.AXES.contains(item) || item instanceof AxeItem) {
            this.toolLists.get("axes").add(i);
            //Genning enchanted axes in common as there's no good way to do it separately without repeating the axe check
            if (EnchantmentHelper.getLevel(Enchantments.FORTUNE, stack) > 0){
                this.toolLists.get("fortAxes").add(i);
            }
            if (EnchantmentHelper.getLevel(Enchantments.SILK_TOUCH, stack) > 0){
                this.toolLists.get("silkAxes").add(i);
            }
        } else if (FabricToolTags.SWORDS.contains(item) || item instanceof SwordItem) {
            this.toolLists.get("swords").add(i);
        }

    }


    /**
     * Change the players selected slot based on the results of findSlot().
     * Checks if there is a slot to change to first.
     * @see autoswitch.Targetable#findSlot()
     * @return If no slot to change to, returns empty Otherwise returns true if the slot changed, false if it didn't
     */
    public Optional<Boolean> changeTool() {
        return findSlot().map(slot -> {
            int currentSlot = this.player.inventory.selectedSlot;
            if (slot == currentSlot) {
                //No need to change slot!
                return Optional.of(false);
            }

            //Loop over it since scrollinhotbar only moves one pos
            for (int i = Math.abs(currentSlot - slot); i > 0; i--){
                this.player.inventory.scrollInHotbar(currentSlot - slot);
            }
            return Optional.of(true); //Slot changed
        }).orElseGet(Optional::empty); //if nothing to change to, return empty

    }


    /**
     * @return returns true if the config allows autoswitch to happen; false otherwise.
     * Does not take into account toggle (doAS)
     */
    protected Boolean switchAllowed() {
        return ((!this.player.isCreative() || this.cfg.switchInCreative()) &&
            (switchTypeAllowed() && (!onMP || this.cfg.switchInMP())));
    }

    //Overrides

    /**
     * Populate the tool map with the right tools for that type based on subclass
     * @param stack itemstack to be checked if it is valid
     * @param i slot of stack, to be inserted into map if it is valid
     */
    abstract void populateTargetTools(ItemStack stack, int i);

    /**
     * Find the optimal tool slot. Return empty if there isn't one
     * @return Returns empty if autoswitch is not allowed or there is no slot to change to
     */
    abstract Optional<Integer> findSlot();


    /**
     * Determine config value for switching for mobs/blocks
     * @return true if that type of switch is allowed in the config
     */
    abstract Boolean switchTypeAllowed();


}

/**
 * Implementation of changeSlot when there is no target. Intended for switchback feature
 */
@SuppressWarnings("WeakerAccess")
class TargetableNone extends Targetable {
    int prevSlot;


    public TargetableNone(int prevSlot, PlayerEntity player) {
        super(player, null, null, null);
        this.prevSlot = prevSlot;
    }

    @Override
    void populateTargetTools(ItemStack stack, int i) {

    }

    @Override
    Optional<Integer> findSlot() {
        return Optional.of(this.prevSlot);
    }

    @Override
    Boolean switchTypeAllowed() {
        return true;
    }
}

/**
 * Used when targeting an entity
 */
@SuppressWarnings("WeakerAccess")
class TargetableEntity extends Targetable {
    private final Entity entity;

    public TargetableEntity(Entity target, PlayerEntity player, AutoSwitchConfig cfg, AutoSwitchMaterialConfig matCfg, Boolean onMP) {
        super(player, cfg, matCfg, onMP);
        populateToolLists(player);
        this.entity = target;
        this.player = player;
    }


    /**
     * Checks against enchants on the stack and TridentItem class
     * @param stack itemstack to be checked if it is valid
     * @param i     slot of stack, to be inserted into map if it is valid
     */
    @Override
    void populateTargetTools(ItemStack stack, int i) {
        Item item = stack.getItem();
        if (EnchantmentHelper.getLevel(Enchantments.BANE_OF_ARTHROPODS, stack) > 0){
            this.toolLists.get("banes").add(i);
        } else if (EnchantmentHelper.getLevel(Enchantments.SMITE, stack) > 0){
            this.toolLists.get("smites").add(i);
        } else if (EnchantmentHelper.getLevel(Enchantments.SHARPNESS, stack) > 0){
            this.toolLists.get("sharps").add(i);
        } else if (EnchantmentHelper.getLevel(Enchantments.IMPALING, stack) > 0){
            this.toolLists.get("impalingTridents").add(i);
        }
        if (item instanceof TridentItem) {
            this.toolLists.get("tridents").add(i);
        }

    }

    /**
     * Find the optimal tool slot. Return empty if there isn't one.
     * Checks against EntityGroups with special case for BoatEntity
     * Ignores non-weapon tools
     * @return Returns empty if autoswitch is not allowed or there is no slot to change to
     */
    @Override
    Optional<Integer> findSlot() {
        if (!switchAllowed()) {return Optional.empty();}
        if (entity instanceof LivingEntity) {
            if (((LivingEntity) entity).getGroup() == EntityGroup.ARTHROPOD) {
                if (!toolLists.get("banes").isEmpty()) {
                    return Optional.of(toolLists.get("banes").get(0));
                }
            }

            if (((LivingEntity) entity).getGroup() == EntityGroup.UNDEAD) {
                if (!toolLists.get("smites").isEmpty()){
                    return Optional.of(toolLists.get("smites").get(0));
                }
            }

            if (((LivingEntity) entity).getGroup() == EntityGroup.AQUATIC) {
                if (!toolLists.get("impalingTridents").isEmpty()){
                    return Optional.of(toolLists.get("impalingTridents").get(0));
                }
            }

        }

        if (entity instanceof BoatEntity) {
            if (!toolLists.get("axes").isEmpty()) {
                return Optional.of(toolLists.get("axes").get(0));
            }
        }

        for (Map.Entry<String, ArrayList<Integer>> toolList : toolLists.entrySet()){
            if (!toolList.getValue().isEmpty()) {
                return Optional.of(toolList.getValue().get(0));
            }
        }

        return Optional.empty();
    }

    @Override
    Boolean switchTypeAllowed() {
        return this.cfg.switchForMobs();
    }
}

/**
 * Targetable instance for targeting a block
 */
@SuppressWarnings("WeakerAccess")
class TargetableMaterial extends Targetable {
    private final Material target;

    public TargetableMaterial(BlockState target, PlayerEntity player, AutoSwitchConfig cfg, AutoSwitchMaterialConfig matCfg, Boolean onMP) {
        super(player, cfg, matCfg, onMP);
        populateToolLists(player);
        this.player = player;
        this.target = target.getMaterial();
    }


    /**
     * Checks against itemclass and enchantments on the stack
     * @param stack itemstack to be checked if it is valid
     * @param i slot of stack, to be inserted into map if it is valid
     */
    @Override
    void populateTargetTools(ItemStack stack, int i) {
        Item item = stack.getItem();
        if (FabricToolTags.PICKAXES.contains(item) || item instanceof PickaxeItem) {
            this.toolLists.get("picks").add(i);
            if (EnchantmentHelper.getLevel(Enchantments.FORTUNE, stack) > 0){
                this.toolLists.get("fortPicks").add(i);
            }
            if (EnchantmentHelper.getLevel(Enchantments.SILK_TOUCH, stack) > 0){
                this.toolLists.get("silkPicks").add(i);
            }
        } else if (FabricToolTags.SHOVELS.contains(item) || item instanceof ShovelItem) {
            this.toolLists.get("shovels").add(i);
            if (EnchantmentHelper.getLevel(Enchantments.SILK_TOUCH, stack) > 0){
                this.toolLists.get("silkShovels").add(i);
            }
        } else if (item instanceof ShearsItem) {
            this.toolLists.get("shears").add(i);
        }

    }

    /**
     * Checks block material against the target lists
     */
    @Override
    Optional<Integer> findSlot() {
        if (!switchAllowed()) {return Optional.empty();}
        for (Map.Entry<String, ArrayList<Integer>> toolList : toolLists.entrySet()){
            if (!toolList.getValue().isEmpty()) {
                if (!toolTargetLists.get(StringUtils.chop(toolList.getKey())).isEmpty()) {
                    if (toolTargetLists.get(StringUtils.chop(toolList.getKey())).contains(target)) {
                        return Optional.of(toolList.getValue().get(0));
                    }
                }

            }
        }

        return Optional.empty();
    }

    @Override
    Boolean switchTypeAllowed() {
        return this.cfg.switchForBlocks();
    }
}
