package liltojustice.trueadaptivemusic.client.mixin;

import liltojustice.trueadaptivemusic.client.TAMClient;
import gvc.gvc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(gvc.class)
public class SoundSystemMixin {
    @Inject(method = "stop()V", at = @At("HEAD"))
    public void init(CallbackInfo ci) {
        TAMClient.INSTANCE.resetSound();
    }
}
