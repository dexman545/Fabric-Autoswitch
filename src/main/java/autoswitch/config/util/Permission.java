package autoswitch.config.util;

/**
 * Default implementation of a config option being enabled for enums, where each ordinal is a more restricted version of
 * another, ie "ALWAYS", "IF_WEEKEND", "NEVER"
 */
public interface Permission {
    /**
     * @return whether the action should be allowed
     */
    default boolean allowed() {
        return true;
    }

}
