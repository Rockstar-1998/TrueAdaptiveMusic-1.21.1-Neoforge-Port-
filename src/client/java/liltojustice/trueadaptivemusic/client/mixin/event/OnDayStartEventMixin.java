package liltojustice.trueadaptivemusic.client.mixin.event;

import liltojustice.trueadaptivemusic.client.TAMClient;
import liltojustice.trueadaptivemusic.client.trigger.event.types.OnDayStartEvent;
import fgo.fgo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(fgo.class)
public class OnDayStartEventMixin {
    @Inject(at = @At("HEAD"), method = "tick()V")
    public void tick(CallbackInfo ci) {
        fgo thisObject = (fgo)(Object)this;
        if (thisObject.world != null && thisObject.world.getTimeOfDay() % 24000L == 0L) {
            TAMClient.INSTANCE.invokeMusicEvent(OnDayStartEvent.class);
        }
    }
}