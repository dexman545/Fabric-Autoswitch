package autoswitch.modmenu;

import io.github.prospector.modmenu.api.ConfigScreenFactory;
import io.github.prospector.modmenu.api.ModMenuApi;

public class ModMenuImpl implements ModMenuApi {
    @Override
    public String getModId() {
        return "autoswitch";
    }

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return null;
    }
}
