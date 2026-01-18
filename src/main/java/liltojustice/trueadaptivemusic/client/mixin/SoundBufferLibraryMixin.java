package liltojustice.trueadaptivemusic.client.mixin;

import liltojustice.trueadaptivemusic.client.sound.AudioStreamHelper;
import liltojustice.trueadaptivemusic.client.sound.instance.AudioFileSoundInstance;
import net.minecraft.client.sounds.AudioStream;
import net.minecraft.client.sounds.SoundBufferLibrary;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.CompletableFuture;

/**
 * Mixin to intercept sound stream loading and provide custom AudioStream
 * for TrueAdaptiveMusic sounds.
 */
@Mixin(SoundBufferLibrary.class)
public class SoundBufferLibraryMixin {

    /**
     * Intercept getStream to provide custom AudioStream for TAM sounds.
     * We check if there's a pending AudioFileSoundInstance - if so, use its custom
     * stream.
     */
    @Inject(method = "getStream", at = @At("HEAD"), cancellable = true)
    private void onGetStream(ResourceLocation location, boolean looping,
            CallbackInfoReturnable<CompletableFuture<AudioStream>> cir) {

        // Check if we have a pending TAM instance - if so, provide custom audio stream
        // The location check is for any trueadaptivemusic sound
        AudioFileSoundInstance instance = AudioStreamHelper.getPendingInstance();

        if (instance != null && location.getNamespace().equals("trueadaptivemusic")) {
            try {
                System.out.println("[TAM-DEBUG] Providing custom audio stream for: " + location);
                AudioStreamHelper.consumePendingInstance();
                CompletableFuture<AudioStream> streamFuture = instance.getAudioStreamFuture();
                cir.setReturnValue(streamFuture);
            } catch (Exception e) {
                System.err.println("[TrueAdaptiveMusic] Failed to get audio stream: " + e.getMessage());
                e.printStackTrace();
                AudioStreamHelper.consumePendingInstance();
            }
        }
    }
}
