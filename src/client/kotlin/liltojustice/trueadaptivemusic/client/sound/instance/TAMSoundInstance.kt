package liltojustice.trueadaptivemusic.client.sound.instance

import liltojustice.trueadaptivemusic.client.TAMClient
import liltojustice.trueadaptivemusic.client.music.pack.MusicLoadException
import liltojustice.trueadaptivemusic.client.sound.FFmpeg
import liltojustice.trueadaptivemusic.client.sound.stream.FFmpegAudioStream
import liltojustice.trueadaptivemusic.client.sound.stream.TruncatedAudioStream
import net.minecraft.client.sounds.AudioStream
import net.minecraft.client.sounds.JOrbisAudioStream
import net.minecraft.client.resources.sounds.Sound
import net.minecraft.client.resources.sounds.SoundInstance
import net.minecraft.client.sounds.SoundManager
import net.minecraft.client.sounds.WeighedSoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.resources.ResourceLocation
import java.io.InputStream

abstract class TAMSoundInstance(
    val isAmbient: Boolean, val looping: Boolean, val loopStartPoint: UInt): SoundInstance {
    var desiredVolume = 1F
    abstract fun getAudioStream(): AudioStream?

    override fun getLocation(): ResourceLocation {
        return ResourceLocation.fromNamespaceAndPath("trueadaptivemusic", "file")
    }

    override fun resolve(soundManager: SoundManager): WeighedSoundEvents? {
        return null
    }

    override fun getSound(): Sound {
        return SoundManager.EMPTY_SOUND
    }

    override fun getSource(): SoundSource {
        return if (isAmbient) SoundSource.AMBIENT else SoundSource.MUSIC
    }

    override fun isLooping(): Boolean {
        return looping
    }

    override fun isRelative(): Boolean {
        return false
    }

    override fun getDelay(): Int {
        return 0
    }

    override fun getVolume(): Float {
        return 1F
    }

    override fun getPitch(): Float {
        return 1F
    }

    override fun getX(): Double {
        return 0.0
    }

    override fun getY(): Double {
        return 0.0
    }

    override fun getZ(): Double {
        return 0.0
    }

    override fun getAttenuation(): SoundInstance.Attenuation {
        return SoundInstance.Attenuation.NONE
    }

    companion object {
        private const val AMBIENT_LUFS = -36
        private const val MUSIC_LUFS = -26

        fun getAudioStream(
            name: String,
            extension: String,
            inputStreamGetter: () -> InputStream,
            isAmbient: Boolean
        ): AudioStream {
            try {
                return if (!TAMClient.hasFFmpeg && extension == "ogg") {
                    TruncatedAudioStream(JOrbisAudioStream(inputStreamGetter()))
                }
                else {
                    val loudnessUnits = if (isAmbient)
                        AMBIENT_LUFS + TAMClient.options.ambienceLoudnessBoost.value.toInt()
                    else
                        MUSIC_LUFS + TAMClient.options.musicLoudnessBoost.value.toInt()
                    TruncatedAudioStream(
                        FFmpegAudioStream(
                            inputStreamGetter(),
                            FFmpeg.getFileAudioFormat(inputStreamGetter()),
                            loudnessUnits
                        )
                    )
                }
            }
            catch (e: Exception) {
                throw MusicLoadException("Failed to load audio stream for '$name'", e)
            }
        }
    }
}
