package dex.autoswitch;

import dex.autoswitch.config.subentries.FeatureConfig;
import dex.autoswitch.engine.data.SwitchRegistry;
import dex.autoswitch.platform.Services;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

public class CommonClass {
    public static void init() {
        // Must call into SwitchRegistry on initialization or
        // tests will fail due to classpath shenanigans
        // So print out mildly useful debug
        for (var selectableType : SwitchRegistry.INSTANCE.getSelectableTypes()) {
            Constants.LOG.info(selectableType.toString());
        }

        if (Services.PLATFORM.isModLoaded("autoswitch")) {
            Constants.LOG.info("Hello to autoswitch");
        }
    }

    /**
     * Update toggle and send message to player if necessary
     *
     * @param player      client's player
     * @param toggle      toggle to update on keybinding press
     * @param keyChooser  choose which translation to use
     * @param tlKeyTruthy message if keyChooser is true
     * @param tlKeyFalsy  message if keyChooser is false
     *
     * @return updated toggle
     */
    public static boolean keybindingToggleAction(Player player, boolean toggle, boolean keyChooser,
                                                 String tlKeyTruthy, String tlKeyFalsy) {
        if (Constants.CONFIG.featureConfig.toggleMessageControl.allowed()) {
            // Toggle message
            var msg = Component.translatable(keyChooser ? tlKeyTruthy : tlKeyFalsy);

            // Display msg above hotbar, set false to display in text chat
            player.displayClientMessage(msg, Constants.CONFIG.featureConfig.toggleMessageControl == FeatureConfig.DisplayControl.DEFAULT);
        }

        return !toggle;
    }
}
