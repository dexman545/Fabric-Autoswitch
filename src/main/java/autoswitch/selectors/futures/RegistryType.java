package autoswitch.selectors.futures;

public enum RegistryType {
    ITEM, ENCHANTMENT, BLOCK, ENTITY, BLOCK_OR_ENTITY {
        @Override
        public boolean matches(RegistryType type) {
            return super.matches(type) || type == BLOCK || type == ENTITY;
        }
    };

    public boolean matches(RegistryType type) {
        return type == this;
    }
}
