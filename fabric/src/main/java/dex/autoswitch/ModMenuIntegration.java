package dex.autoswitch;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import dex.autoswitch.gui.AutoswitchConfigScreen;

/**
 * Exposes {@link AutoswitchConfigScreen} to ModMenu, if it's installed.
 * Entirely optional - AutoSwitch works fine without ModMenu present, this
 * entrypoint simply never gets invoked in that case.
 */
public class ModMenuIntegration implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return AutoswitchConfigScreen::new;
    }
}
