package liltojustice.trueadaptivemusic.client.sound

import liltojustice.trueadaptivemusic.client.mixin.accessor.ChannelAccessor
import liltojustice.trueadaptivemusic.client.mixin.accessor.SoundEngineAccessor
import liltojustice.trueadaptivemusic.client.mixin.accessor.SoundManagerAccessor
import net.minecraft.client.resources.sounds.SoundInstance
import net.minecraft.client.sounds.SoundManager
import org.lwjgl.openal.AL10

/**
 * Helper object for controlling OpenAL source volume directly.
 * This bypasses the normal Minecraft sound system to enable real-time volume changes.
 */
object OpenALVolumeHelper {
    
    /**
     * Sets the volume of a playing sound instance directly via OpenAL.
     * 
     * @param soundManager The Minecraft SoundManager
     * @param soundInstance The sound instance to modify
     * @param volume The target volume (0.0 to 1.0)
     * @return true if volume was successfully set, false otherwise
     */
    fun setSourceVolume(soundManager: SoundManager, soundInstance: SoundInstance, volume: Float): Boolean {
        try {
            // Get SoundEngine from SoundManager
            val soundEngine = (soundManager as SoundManagerAccessor).soundEngine
            
            // Get the instanceToChannel map
            val instanceToChannel = (soundEngine as SoundEngineAccessor).instanceToChannel
            
            // Get the ChannelHandle for this sound instance
            val channelHandle = instanceToChannel[soundInstance] ?: return false
            
            // Execute on the channel to set volume via OpenAL
            channelHandle.execute { channel ->
                val source = (channel as ChannelAccessor).source
                AL10.alSourcef(source, AL10.AL_GAIN, volume.coerceIn(0f, 1f))
            }
            
            return true
        } catch (e: Exception) {
            println("[TAM-DEBUG] OpenALVolumeHelper.setSourceVolume failed: ${e.message}")
            return false
        }
    }
    
    /**
     * Gets the current volume of a playing sound instance from OpenAL.
     * 
     * @param soundManager The Minecraft SoundManager
     * @param soundInstance The sound instance to query
     * @return The current volume, or -1 if unable to retrieve
     */
    fun getSourceVolume(soundManager: SoundManager, soundInstance: SoundInstance): Float {
        try {
            val soundEngine = (soundManager as SoundManagerAccessor).soundEngine
            val instanceToChannel = (soundEngine as SoundEngineAccessor).instanceToChannel
            val channelHandle = instanceToChannel[soundInstance] ?: return -1f
            
            var volume = -1f
            channelHandle.execute { channel ->
                val source = (channel as ChannelAccessor).source
                volume = AL10.alGetSourcef(source, AL10.AL_GAIN)
            }
            
            return volume
        } catch (e: Exception) {
            println("[TAM-DEBUG] OpenALVolumeHelper.getSourceVolume failed: ${e.message}")
            return -1f
        }
    }
}
