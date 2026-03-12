package liltojustice.trueadaptivemusic.client.mixin;

import liltojustice.trueadaptivemusic.client.javasucks.DebugHudMixinHelper;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.Gui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class DebugHudMixin {
    @Inject(
            method = "render",
            at = @At("HEAD"))
    public void render(GuiGraphics context, DeltaTracker deltaTracker, CallbackInfo ci) {
        DebugHudMixinHelper.render(context);
    }
}
