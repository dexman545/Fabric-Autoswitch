package dex.autoswitch.gui;

import dex.autoswitch.Constants;
import dex.autoswitch.config.subentries.FeatureConfig;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * ModMenu config screen for AutoSwitch.
 * <p>
 * Covers the most commonly requested tweaks (see
 * <a href="https://github.com/dexman545/Fabric-Autoswitch/issues/88">#88</a>
 * for the delay/configurability request this responds to) without needing
 * a third-party config GUI library - just vanilla widgets, written back to
 * {@code autoswitch.conf} through {@link Constants#updateConfig}.
 */
public class AutoswitchConfigScreen extends Screen {

    private final Screen parent;

    // Working copies, committed back to the config only on "Save".
    private boolean disableOnStartup;
    private boolean disableWhenCrouched;
    private boolean switchInCreative;
    private boolean switchInMp;
    private boolean preferMinimumViableTool;
    private boolean skipDepletedItems;
    private boolean preserveDamagedTools;
    private FeatureConfig.DisplayControl toggleMessageControl;

    private EditBox switchbackDelayField;
    private EditBox preventBlockSwitchField;

    public AutoswitchConfigScreen(Screen parent) {
        super(Component.translatable("autoswitch.config.title"));
        this.parent = parent;

        var fc = Constants.CONFIG.featureConfig;
        this.disableOnStartup = fc.disableOnStartup;
        this.disableWhenCrouched = fc.disableWhenCrouched;
        this.switchInCreative = fc.switchInCreative;
        this.switchInMp = fc.switchInMp;
        this.preferMinimumViableTool = fc.preferMinimumViableTool;
        this.skipDepletedItems = fc.skipDepletedItems;
        this.preserveDamagedTools = fc.preserveDamagedTools;
        this.toggleMessageControl = fc.toggleMessageControl;
    }

    @Override
    protected void init() {
        var fc = Constants.CONFIG.featureConfig;

        int leftX = this.width / 2 - 160;
        int rightX = this.width / 2 + 10;
        int colWidth = 150;
        int rowHeight = 22;

        int y = 32;
        this.addRenderableWidget(Checkbox.builder(Component.translatable("autoswitch.config.disable-on-startup"), this.font)
                .pos(leftX, y).selected(disableOnStartup)
                .onValueChange((cb, v) -> disableOnStartup = v)
                .build());
        y += rowHeight;
        this.addRenderableWidget(Checkbox.builder(Component.translatable("autoswitch.config.disable-when-crouched"), this.font)
                .pos(leftX, y).selected(disableWhenCrouched)
                .onValueChange((cb, v) -> disableWhenCrouched = v)
                .build());
        y += rowHeight;
        this.addRenderableWidget(Checkbox.builder(Component.translatable("autoswitch.config.switch-in-creative"), this.font)
                .pos(leftX, y).selected(switchInCreative)
                .onValueChange((cb, v) -> switchInCreative = v)
                .build());
        y += rowHeight;
        this.addRenderableWidget(Checkbox.builder(Component.translatable("autoswitch.config.switch-in-mp"), this.font)
                .pos(leftX, y).selected(switchInMp)
                .onValueChange((cb, v) -> switchInMp = v)
                .build());
        y += rowHeight;
        this.addRenderableWidget(Checkbox.builder(Component.translatable("autoswitch.config.prefer-minimum-viable-tool"), this.font)
                .pos(leftX, y).selected(preferMinimumViableTool)
                .onValueChange((cb, v) -> preferMinimumViableTool = v)
                .build());
        y += rowHeight;
        this.addRenderableWidget(Checkbox.builder(Component.translatable("autoswitch.config.skip-depleted-items"), this.font)
                .pos(leftX, y).selected(skipDepletedItems)
                .onValueChange((cb, v) -> skipDepletedItems = v)
                .build());
        y += rowHeight;
        this.addRenderableWidget(Checkbox.builder(Component.translatable("autoswitch.config.preserve-damaged-tools"), this.font)
                .pos(leftX, y).selected(preserveDamagedTools)
                .onValueChange((cb, v) -> preserveDamagedTools = v)
                .build());

        int ry = 32;
        this.addRenderableWidget(CycleButton.builder((FeatureConfig.DisplayControl d) ->
                        Component.translatable("autoswitch.config.toggle-message." + d.name().toLowerCase()), toggleMessageControl)
                .withValues(FeatureConfig.DisplayControl.values())
                .create(rightX, ry, colWidth, 20, Component.translatable("autoswitch.config.toggle-message"),
                        (btn, v) -> toggleMessageControl = v));
        ry += rowHeight + 14;

        this.addRenderableWidget(new StringWidget(rightX, ry, colWidth, 12,
                Component.translatable("autoswitch.config.switchback-delay"), this.font));
        ry += 12;
        switchbackDelayField = new EditBox(this.font, rightX, ry, colWidth, 16, Component.translatable("autoswitch.config.switchback-delay"));
        switchbackDelayField.setMaxLength(4);
        switchbackDelayField.setValue(String.valueOf(fc.switchbackDelay));
        this.addRenderableWidget(switchbackDelayField);

        ry += rowHeight + 10;
        this.addRenderableWidget(new StringWidget(rightX, ry, colWidth, 24,
                Component.translatable("autoswitch.config.prevent-block-switch-after-entity-attack"), this.font));
        ry += 24;
        preventBlockSwitchField = new EditBox(this.font, rightX, ry, colWidth, 16,
                Component.translatable("autoswitch.config.prevent-block-switch-after-entity-attack"));
        preventBlockSwitchField.setMaxLength(4);
        preventBlockSwitchField.setValue(String.valueOf(fc.preventBlockSwitchAfterEntityAttack));
        this.addRenderableWidget(preventBlockSwitchField);

        int buttonY = this.height - 28;
        int buttonWidth = 150;
        this.addRenderableWidget(Button.builder(Component.translatable("autoswitch.config.save"), b -> onSave())
                .bounds(this.width / 2 - buttonWidth - 5, buttonY, buttonWidth, 20)
                .build());
        this.addRenderableWidget(Button.builder(Component.translatable("gui.cancel"), b -> {
                    if (this.minecraft != null) {
                        this.minecraft.setScreenAndShow(parent);
                    }
                })
                .bounds(this.width / 2 + 5, buttonY, buttonWidth, 20)
                .build());

        super.init();
    }

    private void onSave() {
        int switchbackDelay = parseIntOr(switchbackDelayField.getValue(), Constants.CONFIG.featureConfig.switchbackDelay);
        int preventBlockDelay = parseIntOr(preventBlockSwitchField.getValue(), Constants.CONFIG.featureConfig.preventBlockSwitchAfterEntityAttack);

        Constants.updateConfig(cfg -> {
            var fc = cfg.featureConfig;
            fc.disableOnStartup = disableOnStartup;
            fc.disableWhenCrouched = disableWhenCrouched;
            fc.switchInCreative = switchInCreative;
            fc.switchInMp = switchInMp;
            fc.preferMinimumViableTool = preferMinimumViableTool;
            fc.skipDepletedItems = skipDepletedItems;
            fc.preserveDamagedTools = preserveDamagedTools;
            fc.toggleMessageControl = toggleMessageControl;
            fc.switchbackDelay = Math.max(0, switchbackDelay);
            fc.preventBlockSwitchAfterEntityAttack = Math.max(0, preventBlockDelay);
            return cfg;
        });

        if (this.minecraft != null) {
            this.minecraft.setScreenAndShow(parent);
        }
    }

    private static int parseIntOr(String s, int fallback) {
        try {
            return Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.setScreenAndShow(parent);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
