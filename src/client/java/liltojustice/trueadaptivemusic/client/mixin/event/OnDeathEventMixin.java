package liltojustice.trueadaptivemusic.client.mixin.event;

import liltojustice.trueadaptivemusic.client.TAMClient;
import liltojustice.trueadaptivemusic.client.trigger.event.types.OnDeathEvent;
import fnh.fnh;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(fnh.class)
public class OnDeathEventMixin {
    @Inject(at = @At("HEAD"), method = "init()V")
    public void init(CallbackInfo ci) {
        TAMClient.INSTANCE.invokeMusicEvent(OnDeathEvent.class);
    }
}