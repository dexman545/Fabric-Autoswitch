package dex.autoswitch.config.subentries;

import java.util.HashSet;
import java.util.Set;

import dex.autoswitch.config.data.tree.ExpressionTree;
import dex.autoswitch.engine.Action;
import dex.autoswitch.engine.TargetType;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;

@ConfigSerializable
public class FeatureConfig {
    @Comment("""
            Switch away from the currently active tool when no tools matched for the current target.
            E.g. you are holding a shovel but attack stone and have no pickaxe, if this is enabled
            it will switch off of the shovel to some non-tool item.\
            """)
    public boolean switchAwayFromTools = true;

    @Comment("""
            "Prefer the tool with minimum required mining level.\
            """)
    public boolean preferMinimumViableTool = true;

    public Set<TargetType> switchAllowed = Set.of(TargetType.values());

    @Comment("""
            Return to the previous slot when no longer performing the action on the specified type. Leave blank\
             to disable this behavior entirely.""")
    public Set<TargetType> switchBackAllowed = Set.of(TargetType.BLOCKS, TargetType.ENTITIES);

    @Comment("""
            Which actions to perform switching for.\
            """)
    public Set<Action> switchActions = Set.of(Action.values());

    @Comment("""
            Disable switching when crouching.\
            """)
    public boolean disableWhenCrouched = false;

    @Comment("""
            Disable switching on startup.\
            """)
    public boolean disableOnStartup = false;

    @Comment("""
            Don't switch tools in creative mode.\
            """)
    public boolean switchInCreative = false;

    @Comment("""
            Switch in multiplayer.\
            """)
    public boolean switchInMp = true;

    @Comment("""
            Controls where and if the keybinding toggle message should be displayed. DEFAULT is above the hotbar,\
             like with bed messages. CHAT is in the chat bar, like a normal chat message. Set to OFF to disable\
             the message entirely.\
            """)
    public DisplayControl toggleMessageControl = DisplayControl.DEFAULT;

    @Comment("""
            Prevents switching for 'attack' action on a block for the specified number of ticks \
            after attacking an entity.\
            """)
    public int preventBlockSwitchAfterEntityAttack = 5;

    @Comment("""
            Delay in ticks from end of hand swinging to perform switchback action.\
            """)
    public int switchbackDelay = 1;

    @Comment("""
            Delay switchback until attack progress has fully charged when these targets match.
            This is useful when fighting mobs, where otherwise after each attack switchback will trigger,
            causing the attack progress to reset.
            """)
    public Set<SwitchbackSelector> switchbackWaitsForAttackProgress = new HashSet<>();

    @Comment("""
            Determine when to swap a tool to the offhand when interacting.\
            """)
    public Set<OffhandSelector> offhandSelectors = new HashSet<>();

    @Comment("""
            If true, skip tools that are out of energy or durability.\
            """)
    public boolean skipDepletedItems = true;

    public enum DisplayControl {
        DEFAULT(true), CHAT(true),
        OFF(false);

        private final boolean allowed;

        DisplayControl(boolean allowed1) {
            this.allowed = allowed1;
        }

        public boolean allowed() {
            return allowed;
        }
    }

    @ConfigSerializable
    public static class OffhandSelector {
        public int priority = 10;
        public TargetType type;
        public ExpressionTree tool;

        @Override
        public String toString() {
            return "OffhandSelector{" +
                    "priority=" + priority +
                    ", type=" + type +
                    ", tool=" + tool +
                    '}';
        }
    }

    @ConfigSerializable
    public static class SwitchbackSelector {
        public Action action;
        public ExpressionTree target;

        @Override
        public String toString() {
            return "SwitchbackSelector{" +
                    "action=" + action +
                    ", target=" + target +
                    '}';
        }
    }
}
