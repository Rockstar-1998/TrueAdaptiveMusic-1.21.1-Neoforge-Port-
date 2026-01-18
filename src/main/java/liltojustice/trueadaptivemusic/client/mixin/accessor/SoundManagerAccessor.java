package liltojustice.trueadaptivemusic.client.mixin.accessor;

import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.client.sounds.SoundManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Accessor mixin for SoundManager to access the SoundEngine.
 * This allows us to get the SoundEngine from SoundManager for volume control.
 */
@Mixin(SoundManager.class)
public interface SoundManagerAccessor {
    @Accessor("soundEngine")
    SoundEngine getSoundEngine();
}
