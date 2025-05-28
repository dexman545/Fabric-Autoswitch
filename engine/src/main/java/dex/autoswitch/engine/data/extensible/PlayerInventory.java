package dex.autoswitch.engine.data.extensible;

public interface PlayerInventory<ITEM> {
    void selectSlot(int slot);

    int currentSelectedSlot();

    /**
     * @return the maximum number of slots to search
     */
    int slotCount();

    ITEM getTool(int slot);

    boolean canSwitchBack();

    void moveOffhand();
}
