package liltojustice.trueadaptivemusic.client.sound.playable

import net.minecraft.client.resources.sounds.SoundInstance

interface PlayableSound {
    fun makeSoundInstance(): SoundInstance
    fun getSoundName(): String
}