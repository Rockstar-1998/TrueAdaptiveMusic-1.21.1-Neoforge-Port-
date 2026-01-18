package liltojustice.trueadaptivemusic.client.mixin;

import liltojustice.trueadaptivemusic.client.TAMClient;
import net.minecraft.client.sounds.SoundManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * This Mixin prevents vanilla pause behavior for TAM-managed sounds.
 * In NeoForge, we use a simplified approach that just prevents the pause
 * if TAM has active sound instances.
 */
@Mixin(SoundManager.class)
public class SoundManagerPauseAllMixin {
    @Inject(method = "pause()V", at = @At("HEAD"), cancellable = true)
    public void pause(CallbackInfo ci) {
        // If TAM is managing music, don't pause all sounds
        if (TAMClient.INSTANCE.getMusicPack() != null) {
            ci.cancel();
        }
    }
}
