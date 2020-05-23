package autoswitch.modmenu;

import autoswitch.AutoSwitch;
import io.github.prospector.modmenu.api.ConfigScreenFactory;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.TranslatableText;

@Environment(EnvType.CLIENT)
public class ASConfigScreenFactory implements ConfigScreenFactory {
    @Override
    public Screen create(Screen parent) {
        if (AutoSwitch.cfg.disableModMenuConfig()) {
            return null;
        }

        try {
            return new ASConfigScreen(new TranslatableText("screen.autoswitch.config"));
        } catch (Exception e) {
            AutoSwitch.logger.error("Failed to create modmenu screen!");
            AutoSwitch.logger.error(e);
        }

        return null;
    }
}
