package liltojustice.trueadaptivemusic.client.sound.instance

import liltojustice.trueadaptivemusic.Constants
import liltojustice.trueadaptivemusic.client.music.MusicLoadException
import liltojustice.trueadaptivemusic.client.sound.FFmpeg
import liltojustice.trueadaptivemusic.client.sound.file.SoundFile
import liltojustice.trueadaptivemusic.client.sound.stream.TruncatedAudioStream
import net.minecraft.client.resources.sounds.AbstractSoundInstance
import net.minecraft.client.resources.sounds.SoundInstance
import net.minecraft.client.sounds.AudioStream
import net.minecraft.client.sounds.SoundBufferLibrary
import net.minecraft.sounds.SoundSource
import net.minecraft.resources.ResourceLocation
import java.util.concurrent.CompletableFuture

class AudioFileSoundInstance(private val soundFile: SoundFile)
    : AbstractSoundInstance(Constants.AUDIO_FILE_STREAM_ID, SoundSource.MUSIC, SoundInstance.createUnseededRandom()),
    VolumeControlled {
    
    fun getAudioStreamFuture(): CompletableFuture<AudioStream> {
        val extension = soundFile.getExtension()
        try {
            return if (extension == "ogg") {
                CompletableFuture.supplyAsync {
                    TruncatedAudioStream(net.minecraft.client.sounds.JOrbisAudioStream(soundFile.getInputStream()))
                }
            } else {
                CompletableFuture.completedFuture(TruncatedAudioStream(FFmpeg.makeStream(soundFile)))
            }
        }
        catch (e: Exception) {
            throw MusicLoadException("Failed to play sound file '${soundFile.getName()}'")
        }
    }

    override fun getVolume(): Float {
        return volume
    }

    override fun setVolume(newVolume: Float) {
        this.volume = newVolume
    }
}
