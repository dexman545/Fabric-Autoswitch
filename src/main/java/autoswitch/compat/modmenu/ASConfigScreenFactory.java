package autoswitch.compat.modmenu;

import autoswitch.AutoSwitch;
import io.github.prospector.modmenu.api.ConfigScreenFactory;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.TranslatableText;

@Environment(EnvType.CLIENT)
class ASConfigScreenFactory implements ConfigScreenFactory<ASConfigScreen> {
    @Override
    public ASConfigScreen create(Screen parent) {
        if (AutoSwitch.cfg.disableModMenuConfig()) {
            return null;
        }

        try {
            return new ASConfigScreen(new TranslatableText("screen.autoswitch.config"));
        } catch (Throwable e) {
            AutoSwitch.logger.error("Failed to create modmenu screen!");
            AutoSwitch.logger.error(e);
        }

        return null;
    }
}
