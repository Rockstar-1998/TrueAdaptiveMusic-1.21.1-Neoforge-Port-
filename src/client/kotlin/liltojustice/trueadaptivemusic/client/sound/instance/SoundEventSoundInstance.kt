package liltojustice.trueadaptivemusic.client.sound.instance

import net.minecraft.client.Minecraft
import net.minecraft.client.sounds.AudioStream
import net.minecraft.client.resources.sounds.Sound
import net.minecraft.client.sounds.SoundManager
import net.minecraft.sounds.SoundEvent
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.RandomSource

class SoundEventSoundInstance(
    identifier: ResourceLocation,
    isAmbient: Boolean,
    isLooping: Boolean
): TAMSoundInstance(isAmbient, isLooping, 0U) {
    private val soundManager: SoundManager = Minecraft.getInstance().soundManager
    private val soundEvent = SoundEvent.createVariableRangeEvent(identifier)
    private val soundSet = soundManager.getSoundEvent(identifier)
    private val sound: Sound? = soundSet?.getSound(random)
        ?.takeIf { it != SoundManager.EMPTY_SOUND && it != SoundManager.INTENTIONALLY_EMPTY_SOUND }

    init {
    }

    override fun getAudioStream(): AudioStream? {
        val sound = sound ?: return null
        val resourceManager = Minecraft.getInstance().resourceManager
        val inputStreamGetter = {
            val resource = resourceManager.getResourceStack(sound.path).firstOrNull()
                ?: throw IllegalStateException("Missing sound resource ${sound.path}")
            resource.open()
        }

        return getAudioStream(sound.location.toString(), "ogg", inputStreamGetter, isAmbient)
    }

    override fun getLocation(): ResourceLocation {
        return soundEvent.location
    }

    override fun resolve(soundManager: SoundManager): net.minecraft.client.sounds.WeighedSoundEvents? {
        return soundManager.getSoundEvent(soundEvent.location)
    }

    override fun getSound(): Sound {
        return sound ?: SoundManager.EMPTY_SOUND
    }

    companion object {
        val random = RandomSource.create()
    }
}
