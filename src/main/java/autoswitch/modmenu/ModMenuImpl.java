package autoswitch.modmenu;

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
        return new ASConfigScreenFactory();
    }
}
