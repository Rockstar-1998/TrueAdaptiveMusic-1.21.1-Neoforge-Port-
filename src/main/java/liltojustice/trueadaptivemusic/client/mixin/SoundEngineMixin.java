package liltojustice.trueadaptivemusic.client.mixin;

import liltojustice.trueadaptivemusic.client.sound.AudioStreamHelper;
import liltojustice.trueadaptivemusic.client.sound.instance.AudioFileSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin to intercept sound playback and set up custom AudioStream
 * for AudioFileSoundInstance before playback occurs.
 */
@Mixin(SoundEngine.class)
public abstract class SoundEngineMixin {

    @Inject(method = "play", at = @At("HEAD"))
    private void onPlay(SoundInstance soundInstance, CallbackInfo ci) {
        System.out.println("[TAM-DEBUG] SoundEngineMixin.onPlay called for: " + soundInstance.getClass().getName());

        if (soundInstance instanceof AudioFileSoundInstance audioInstance) {
            System.out.println("[TAM-DEBUG] AudioFileSoundInstance detected! Setting pending instance.");
            // Set the pending instance so SoundBufferLibraryMixin can provide the stream
            AudioStreamHelper.setPendingInstance(audioInstance);
        }
    }
}
