package liltojustice.trueadaptivemusic.client.sound

import liltojustice.trueadaptivemusic.client.sound.instance.VolumeControlled
import net.minecraft.client.OptionInstance
import net.minecraft.client.resources.sounds.SoundInstance
import net.minecraft.client.sounds.SoundManager

/**
 * Extension functions for SoundManager.
 * Uses OpenAL direct volume control for real-time fade effects.
 */

fun SoundManager.setInstanceVolume(
    soundInstance: SoundInstance?, volume: Float, volumeOption: OptionInstance<Double>): Boolean {
    if (soundInstance == null) return false
    
    val adjustedVolume = volume * volumeOption.get().toFloat()
    
    // Try OpenAL direct control first for real-time updates
    val openALSuccess = OpenALVolumeHelper.setSourceVolume(this, soundInstance, adjustedVolume)
    
    // Also update the instance property for consistency
    (soundInstance as? VolumeControlled)?.setVolume(adjustedVolume)
    
    return openALSuccess
}

fun SoundManager.resumeInstance(soundInstance: SoundInstance?): Boolean {
    if (soundInstance == null) return false
    this.resume()
    return true
}

fun SoundManager.pauseInstance(soundInstance: SoundInstance?): Boolean {
    if (soundInstance == null) return false
    this.pause()
    return true
}
