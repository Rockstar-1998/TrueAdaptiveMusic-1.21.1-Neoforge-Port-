package liltojustice.trueadaptivemusic.client.mixin;

import liltojustice.trueadaptivemusic.client.javasucks.DebugHudMixinHelper;
import fhz.fhz;
import fhy.fhy;
import fgf.fgf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(fhy.class)
public class DebugHudMixin {
    @Inject(
            method = "render(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/client/render/RenderTickCounter;)V",
            at = @At("HEAD"))
    public void render(fhz context, fgf tickCounter, CallbackInfo ci) {
        DebugHudMixinHelper.render(context);
    }
}
