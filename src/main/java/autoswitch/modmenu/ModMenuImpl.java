package autoswitch.modmenu;

import autoswitch.AutoSwitch;
import io.github.prospector.modmenu.api.ConfigScreenFactory;
import io.github.prospector.modmenu.api.ModMenuApi;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class ModMenuImpl implements ModMenuApi {
    @Override
    public String getModId() {
        return "autoswitch";
    }

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        try {
            return new ASConfigScreenFactory();
        } catch (Exception e) {
            AutoSwitch.logger.error("Failed to make ModMenu screen for AutoSwitch");
            AutoSwitch.logger.error(e);
        }

        return null;
    }
}
