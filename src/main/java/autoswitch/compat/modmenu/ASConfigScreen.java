package autoswitch.compat.modmenu;

import java.awt.Color;
import java.io.File;

import autoswitch.AutoSwitch;

import net.fabricmc.loader.api.FabricLoader;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Util;

class ASConfigScreen extends Screen {

    private ButtonWidget openFolder;
    private ButtonWidget openConfigFiles;

    ASConfigScreen(Text title) {
        super(title);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        try {
            this.renderBackground(matrices);
            this.fillGradient(matrices, 0, 0, this.width, this.height, Color.GRAY.getRGB(), Color.GRAY.getRGB());
            int centerX = this.width / 2;
            int centerY = this.width / 2;

            for (int i = 1; i < 6; i++) {
                drawCenteredText(matrices, this.textRenderer, new TranslatableText("msg.autoswitch.config." + i),
                                 centerX, centerY / 3 + ((i) * 10), Color.RED.getRGB());
            }

            super.render(matrices, mouseX, mouseY, delta);

        } catch (Throwable e) {
            AutoSwitch.logger.error("Failed to render screen!");
            AutoSwitch.logger.error(e);
        }

    }

    @Override
    // Temporary screen until a proper one is made. See https:// github.com/dexman545/Fabric-Autoswitch/issues/23
    protected void init() {
        try {
            this.openFolder = this.addSelectableChild(new ButtonWidget(this.width / 2 - 200, 2 * this.height / 3, 150, 20,
                                                              new TranslatableText("button.autoswitch.openfolder"),
                                                              (button -> Util.getOperatingSystem()
                                                                             .open(FabricLoader.getInstance()
                                                                                               .getConfigDir()
                                                                                               .toString()))) {
                protected MutableText getNarrationMessage() {
                    return new TranslatableText("button.autoswitch.openfolder");
                }

                public Text getMessage() {
                    return new TranslatableText("button.autoswitch.openfolder");
                }
            });

            this.openConfigFiles = this.addSelectableChild(new ButtonWidget(this.width / 2 + 50, 2 * this.height / 3, 150, 20,
                                                                   new TranslatableText(
                                                                           "button.autoswitch.openconfigs"),
                                                                   (button -> {
                                                                       Util.getOperatingSystem().open(new File(
                                                                               FabricLoader.getInstance().getConfigDir()
                                                                                           .toString(),
                                                                               "autoswitch.cfg"));
                                                                       Util.getOperatingSystem().open(new File(
                                                                               FabricLoader.getInstance().getConfigDir()
                                                                                           .toString(),
                                                                               "autoswitchMaterials.cfg"));
                                                                   })) {
                protected MutableText getNarrationMessage() {
                    return new TranslatableText("button.autoswitch.openconfigs");
                }

                public Text getMessage() {
                    return new TranslatableText("button.autoswitch.openconfigs");
                }
            });

        } catch (Throwable e) {
            AutoSwitch.logger.error("Failed to initialize screen!");
            AutoSwitch.logger.error(e);
        }

    }

}
