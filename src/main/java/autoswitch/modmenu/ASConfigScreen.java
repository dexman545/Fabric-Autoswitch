package autoswitch.modmenu;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.screen.world.CreateWorldScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

public class ASConfigScreen extends Screen {

    ButtonWidget closeButton;
    TextFieldWidget test;

    protected ASConfigScreen(Text title) {
        super(title);
    }

    @Override
    protected void init() {
        renderBackgroundTexture(0);
        this.closeButton = this.addButton(new ButtonWidget(this.width / 2 - 155, 151, 150, 20, new TranslatableText("selectWorld.allowCommands"), (buttonWidget) -> {
            buttonWidget.queueNarration(250);
        }) {
            public Text getMessage() {
                return super.getMessage().shallowCopy().append(" ").append(ScreenTexts.getToggleText(false));
            }

            protected MutableText getNarrationMessage() {
                return super.getNarrationMessage().append(". ").append(new TranslatableText("selectWorld.allowCommands.info"));
            }
        });

        this.test = new TextFieldWidget(this.textRenderer, this.width / 2 - 100, 60, 200, 20, new TranslatableText("selectWorld.enterName")) {
            protected MutableText getNarrationMessage() {
                return super.getNarrationMessage().append(". ").append(new TranslatableText("selectWorld.resultFolder")).append(" ");
            }
        };
        this.test.setText("test");
        this.test.setChangedListener((string) -> {
            //this.levelName = string;
            //this.createLevelButton.active = !this.test.getText().isEmpty();
            //this.updateSaveFolderName();
        });
        this.children.add(this.test);
    }

    @Override
    public void renderBackgroundTexture(int vOffset) {
        super.renderBackgroundTexture(vOffset);
    }
}
