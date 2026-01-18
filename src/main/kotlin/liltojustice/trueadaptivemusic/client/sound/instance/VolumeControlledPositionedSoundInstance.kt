package liltojustice.trueadaptivemusic.client.sound.instance

import net.minecraft.client.resources.sounds.SimpleSoundInstance
import net.minecraft.client.resources.sounds.SoundInstance
import net.minecraft.sounds.SoundSource
import net.minecraft.sounds.SoundEvent

class VolumeControlledPositionedSoundInstance(sound: SoundEvent)
    : VolumeControlled,
    SimpleSoundInstance(
        sound.location,
        SoundSource.MUSIC,
        1.0f,
        1.0f,
        SoundInstance.createUnseededRandom(),
        false,
        0,
        SoundInstance.Attenuation.NONE,
        0.0,
        0.0,
        0.0,
        true
    ) {

    override fun getVolume(): Float {
        return volume
    }

    override fun setVolume(newVolume: Float) {
        this.volume = newVolume
    }
}