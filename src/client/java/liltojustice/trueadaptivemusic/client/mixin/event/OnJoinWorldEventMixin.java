package liltojustice.trueadaptivemusic.client.mixin.event;

import liltojustice.trueadaptivemusic.client.TAMClient;
import liltojustice.trueadaptivemusic.client.trigger.event.types.OnJoinWorldEvent;
import fzg.fzg;
import adl.adl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(fzg.class)
public class OnJoinWorldEventMixin {
    @Inject(at = @At("TAIL"), method = "onGameJoin")
    public void onGameJoin(adl packet, CallbackInfo ci) {
        TAMClient.INSTANCE.invokeMusicEvent(OnJoinWorldEvent.class);
    }
}