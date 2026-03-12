package liltojustice.trueadaptivemusic.client.mixin;

import liltojustice.trueadaptivemusic.client.TAMClient;
import fgo.fgo;
import ezf.ezf;
import gvf.gvf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ezf.class)
public class SoundEngineMixin {
    @Inject(method = "init", at = @At("HEAD"))
    public void init(String deviceSpecifier, boolean directionalAudio, CallbackInfo ci) {
        ezf thisObject = (ezf)(Object)this;
        gvf soundManager = fgo.getInstance().getSoundManager();
        if (thisObject == soundManager.soundSystem.soundEngine) {
            TAMClient.INSTANCE.resetSound();
        }
    }
}
