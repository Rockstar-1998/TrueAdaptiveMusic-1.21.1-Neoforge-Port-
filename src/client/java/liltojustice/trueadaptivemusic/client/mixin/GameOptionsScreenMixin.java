package liltojustice.trueadaptivemusic.client.mixin;

import liltojustice.trueadaptivemusic.client.TrueAdaptiveMusicOptionCallback;
import fgo.fgo;
import frh.frh;
import frj.frj;
import fgr.fgr;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(frh.class)
public class GameOptionsScreenMixin {
    @Inject(method = "init()V", at = @At("TAIL"))
    protected void init(CallbackInfo ci) {
        frh gameOptionsScreen = (frh)(Object)this;
        if (!(gameOptionsScreen instanceof frj thisObject)) {
            return;
        }

        var trueAdaptiveMusicButton = new SimpleOption<>(
                "trueadaptivemusic",
                fgr.emptyTooltip(),
                (optionText, value) -> optionText,
                new TrueAdaptiveMusicOptionCallback<>(fgo.getInstance()),
                "",
                option -> {}
        );

        if (thisObject.body != null) {
            thisObject.body.addSingleOptionEntry(trueAdaptiveMusicButton);
        }
    }
}