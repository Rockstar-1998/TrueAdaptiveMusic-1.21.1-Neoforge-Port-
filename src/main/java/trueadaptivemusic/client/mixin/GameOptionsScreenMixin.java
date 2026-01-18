package liltojustice.trueadaptivemusic.client.mixin;

import liltojustice.trueadaptivemusic.client.TrueAdaptiveMusicOptionCallback;
import liltojustice.trueadaptivemusic.client.mixin.accessor.OptionsSubScreenAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.client.gui.screens.options.SoundOptionsScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(OptionsSubScreen.class)
public class GameOptionsScreenMixin {
    @Inject(method = "init()V", at = @At("TAIL"))
    protected void init(CallbackInfo ci) {
        OptionsSubScreen optionsSubScreen = (OptionsSubScreen) (Object) this;
        if (!(optionsSubScreen instanceof SoundOptionsScreen thisObject)) {
            return;
        }

        var trueAdaptiveMusicButton = new OptionInstance<>(
                "True Adaptive Music",
                OptionInstance.noTooltip(),
                (optionText, value) -> optionText,
                new TrueAdaptiveMusicOptionCallback<>(Minecraft.getInstance()),
                "",
                option -> {
                });

        var list = ((OptionsSubScreenAccessor) thisObject).getList();
        if (list != null) {
            list.addSmall(trueAdaptiveMusicButton, null);
        }
    }
}