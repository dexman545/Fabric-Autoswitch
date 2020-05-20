package autoswitch.modmenu;

import io.github.prospector.modmenu.api.ConfigScreenFactory;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.TranslatableText;

@Environment(EnvType.CLIENT)
public class ASConfigScreenFactory implements ConfigScreenFactory {
    @Override
    public Screen create(Screen parent) {
        return new ASConfigScreen(new TranslatableText("screen.asconfig"));
    }
}
