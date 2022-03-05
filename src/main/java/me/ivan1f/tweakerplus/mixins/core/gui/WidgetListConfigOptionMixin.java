package me.ivan1f.tweakerplus.mixins.core.gui;

import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.config.IConfigBoolean;
import fi.dy.masa.malilib.config.IConfigResettable;
import fi.dy.masa.malilib.gui.GuiConfigsBase;
import fi.dy.masa.malilib.gui.button.ButtonGeneric;
import fi.dy.masa.malilib.gui.button.ConfigButtonKeybind;
import fi.dy.masa.malilib.gui.interfaces.IKeybindConfigGui;
import fi.dy.masa.malilib.gui.widgets.*;
import fi.dy.masa.malilib.hotkeys.*;
import fi.dy.masa.malilib.util.StringUtils;
import me.ivan1f.tweakerplus.config.options.TweakerPlusIConfigBase;
import me.ivan1f.tweakerplus.gui.TweakerPlusConfigGui;
import me.ivan1f.tweakerplus.gui.TweakerPlusOptionLabel;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import java.util.function.Function;

@Mixin(WidgetConfigOption.class)
public abstract class WidgetListConfigOptionMixin extends WidgetConfigOptionBase<GuiConfigsBase.ConfigOptionWrapper> {
    @Shadow(remap = false)
    @Final
    protected IKeybindConfigGui host;

    @Shadow(remap = false)
    protected abstract void addKeybindResetButton(int x, int y, IKeybind keybind, ConfigButtonKeybind buttonHotkey);

    @Unique
    private boolean initialBoolean;

    public WidgetListConfigOptionMixin(int x, int y, int width, int height, WidgetListConfigOptionsBase<?, ?> parent, GuiConfigsBase.ConfigOptionWrapper entry, int listIndex) {
        super(x, y, width, height, parent, entry, listIndex);
    }

    private boolean isTweakerPlusConfigGui() {
        return this.parent instanceof WidgetListConfigOptions && ((WidgetListConfigOptionsAccessor) this.parent).getParent() instanceof TweakerPlusConfigGui;
    }

    private boolean showOriginalTextsThisTime;

    @ModifyArgs(
            method = "addConfigOption",
            at = @At(
                    value = "INVOKE",
                    target = "Lfi/dy/masa/malilib/gui/widgets/WidgetConfigOption;addLabel(IIIII[Ljava/lang/String;)V",
                    remap = false
            ),
            remap = false
    )
    private void useMyBetterOptionLabelForTweakerPlus(Args args, int x_, int y_, float zLevel, int labelWidth, int configWidth, IConfigBase config) {
        if (isTweakerPlusConfigGui()) {
            int x = args.get(0);
            int y = args.get(1);
            int width = args.get(2);
            int height = args.get(3);
            int textColor = args.get(4);
            String[] lines = args.get(5);
            if (lines.length != 1) {
                return;
            }

            args.set(5, null);  // cancel original call

            Function<String, String> modifier = s -> s;
            if (config instanceof TweakerPlusIConfigBase) {
                modifier = ((TweakerPlusIConfigBase) config).getGuiDisplayLineModifier();
            }
            TweakerPlusOptionLabel label = new TweakerPlusOptionLabel(x, y, width, height, textColor, lines, new String[]{config.getName()}, modifier);
            this.addWidget(label);
            this.showOriginalTextsThisTime = label.shouldShowOriginalLines();
        } else {
            this.showOriginalTextsThisTime = false;
        }
    }

    @ModifyArg(
            method = "addConfigOption",
            at = @At(
                    value = "INVOKE",
                    target = "Lfi/dy/masa/malilib/gui/widgets/WidgetConfigOption;addConfigComment(IIIILjava/lang/String;)V",
                    remap = false
            ),
            index = 1,
            remap = false
    )
    private int tweaksCommentHeight_minY(int y) {
        if (this.showOriginalTextsThisTime) {
            y -= 4;
        }
        return y;
    }

    @ModifyArg(
            method = "addConfigOption",
            at = @At(
                    value = "INVOKE",
                    target = "Lfi/dy/masa/malilib/gui/widgets/WidgetConfigOption;addConfigComment(IIIILjava/lang/String;)V",
                    remap = false
            ),
            index = 3,
            remap = false
    )
    private int tweaksCommentHeight_height(int height) {
        if (this.showOriginalTextsThisTime) {
            height += 6;
        }
        return height;
    }

    @Inject(
            method = "addHotkeyConfigElements",
            at = @At(value = "HEAD"),
            remap = false,
            cancellable = true
    )
    private void tweakerPlusCustomConfigGui(int x, int y, int configWidth, String configName, IHotkey config, CallbackInfo ci) {
        if (this.isTweakerPlusConfigGui()) {
            if ((config).getKeybind() instanceof KeybindMulti) {
                this.addButtonAndHotkeyWidgets(x, y, configWidth, config);
                ci.cancel();
            }
        }
    }

    private void addButtonAndHotkeyWidgets(int x, int y, int configWidth, IHotkey config) {
        IKeybind keybind = config.getKeybind();

        int triggerBtnWidth = (configWidth - 24) / 2;
        ButtonGeneric triggerButton = new ButtonGeneric(
                x, y, triggerBtnWidth, 20,
                StringUtils.translate("tweakerplus.gui.trigger_button.text"),
                StringUtils.translate("tweakerplus.gui.trigger_button.hover", config.getName())
        );
        this.addButton(triggerButton, (button, mouseButton) -> {
            IHotkeyCallback callback = ((KeybindMultiAccessor) keybind).getCallback();
            KeyAction activateOn = keybind.getSettings().getActivateOn();
            if (activateOn == KeyAction.BOTH || activateOn == KeyAction.PRESS) {
                callback.onKeyAction(KeyAction.PRESS, keybind);
            }
            if (activateOn == KeyAction.BOTH || activateOn == KeyAction.RELEASE) {
                callback.onKeyAction(KeyAction.RELEASE, keybind);
            }
        });

        x += triggerBtnWidth + 2;
        configWidth -= triggerBtnWidth + 2 + 22;

        ConfigButtonKeybind keybindButton = new ConfigButtonKeybind(x, y, configWidth, 20, keybind, this.host);
        x += configWidth + 2;

        this.addWidget(new WidgetKeybindSettings(x, y, 20, 20, keybind, config.getName(), this.parent, this.host.getDialogHandler()));
        x += 22;

        this.addButton(keybindButton, this.host.getButtonPressListener());
        this.addKeybindResetButton(x, y, keybind, keybindButton);
    }

    @ModifyVariable(
            method = "addBooleanAndHotkeyWidgets",
            at = @At(
                    value = "STORE",
                    ordinal = 0
            ),
            ordinal = 3,
            remap = false
    )
    private int tweakerPlusDynamicBooleanButtonWidth(int booleanBtnWidth, int x, int y, int configWidth, IConfigResettable resettableConfig, IConfigBoolean booleanConfig, IKeybind keybind) {
        if (this.isTweakerPlusConfigGui()) {
            booleanBtnWidth = (configWidth - 24) / 2;
        }
        return booleanBtnWidth;
    }
}
