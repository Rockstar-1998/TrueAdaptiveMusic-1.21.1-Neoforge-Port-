package liltojustice.trueadaptivemusic.client.music

import liltojustice.trueadaptivemusic.Logger
import liltojustice.trueadaptivemusic.client.InvokeMusicEventCallback
import liltojustice.trueadaptivemusic.client.TAMClient
import liltojustice.trueadaptivemusic.client.trigger.event.MusicEvent
import liltojustice.trueadaptivemusic.client.sound.VolumeManager
import liltojustice.trueadaptivemusic.client.sound.playable.PlayableSound
import liltojustice.trueadaptivemusic.client.sound.resumeInstance
import liltojustice.trueadaptivemusic.client.trigger.event.types.OnEnterPredicateEvent
import liltojustice.trueadaptivemusic.client.trigger.predicate.MusicPredicate
import liltojustice.trueadaptivemusic.client.trigger.predicate.MusicPredicateTree
import net.minecraft.client.Minecraft
import net.minecraft.client.OptionInstance
import net.minecraft.client.resources.sounds.SimpleSoundInstance
import net.minecraft.client.resources.sounds.SoundInstance
import net.minecraft.sounds.SoundSource
import net.minecraft.world.InteractionResult
import net.minecraft.world.phys.Vec3
import java.util.*
import kotlin.concurrent.schedule
import kotlin.math.max

class MusicManager(
    private val minecraft: Minecraft) {
    private var musicPack: MusicPack? = null
    private var currentMusicPredicateId: String = ""
    private var oldMusicPredicateId: String = ""
    private var currentSoundInstance: SoundInstance? = null
    private var oldSoundInstance: SoundInstance? = null
    private var musicVolumeOption: OptionInstance<Double> =
        minecraft.options.getSoundSourceOptionInstance(SoundSource.MUSIC)
    private val volumeManager = VolumeManager(minecraft.soundManager, musicVolumeOption)
    private var onDemandSound: PlayableSound? = null
    private var onDemandSoundInstance: SoundInstance? = null
    var playingEvent: MusicEvent? = null
        private set
    private var timedIdentifier = ""
    private var timedIdentifierTimer = Timer()
    private var timedIdentifierTimerTask: TimerTask? = null
    private var shouldResume = false
    private var activeEvents: List<MusicEvent> = emptyList()
    private var keepBackground = false
    private var pauseDone = false

    init {
        InvokeMusicEventCallback.EVENT.register { eventType, args ->
            activeEvents.firstOrNull { event ->
                eventType == event.getTypeName()
                        && runCatching { event.validate(*args) }.getOrNull() == true }
                ?.let { event ->
                    event.playableSounds.randomOrNull()?.let {
                        playNow(it, true)
                    }
                    playingEvent = event
                }

            InteractionResult.PASS
        }
    }

    fun selectMusicPack(musicPack: MusicPack?) {
        stop()
        this.musicPack = musicPack
    }

    fun getMusicPack(): MusicPack? {
        return musicPack
    }

    fun playNow(sound: PlayableSound?, keepBackground: Boolean = false) {
        playingEvent = null
        this.keepBackground = keepBackground
        if (sound == onDemandSound) {
            return
        }

        val targetVolume = if (keepBackground) BACKGROUND_VOLUME else 0F

        if (sound == null) {
            onDemandSoundInstance?.let {
                volumeManager.startFade(
                    it, PLAY_NOW_FADE_TICKS, 0F, true)
            }
            onDemandSound = null
            onDemandSoundInstance = null
            currentSoundInstance?.let {
                volumeManager.startFade(it, PLAY_NOW_FADE_TICKS, 1F)
            }

            return
        }

        minecraft.soundManager.stop(oldSoundInstance)
        currentSoundInstance?.let {
            volumeManager.startFade(it, PLAY_NOW_FADE_TICKS, targetVolume)
        }

        minecraft.soundManager.stop(onDemandSoundInstance)
        onDemandSound = sound
        onDemandSound?.let {
            onDemandSoundInstance = it.makeSoundInstance()
            playInstance(onDemandSoundInstance)
        }
    }

    fun tick() {
        if (minecraft.isPaused && !pauseDone) {
            currentSoundInstance
                ?.let {
                    volumeManager.startFade(
                        it, PAUSE_FADE_TICKS, PAUSE_VOLUME) }
            pauseDone = true
        }
        else if (!minecraft.isPaused && pauseDone) {
            currentSoundInstance
                ?.let {
                    volumeManager.startFade(
                        it, PAUSE_FADE_TICKS, 1F) }

            pauseDone = false
        }

        volumeManager.tick()

        if (onDemandSound != null) {
            if (!minecraft.soundManager.isActive(onDemandSoundInstance)) {
                onDemandSound = null
                onDemandSoundInstance = null
                playingEvent = null
                keepBackground = false
                currentSoundInstance?.let {
                    volumeManager.startFade(it, PLAY_NOW_FADE_TICKS, 1F)
                }
            }

            if (!keepBackground) {
                return
            }
        }

        val predicateResult: MusicPredicateTree.Result? = musicPack?.rules?.getMusicToPlay(minecraft)
        val predicatePath = predicateResult?.path ?: ""
        val parameters = predicateResult?.predicate?.parameters ?: MusicPredicate.Parameters()
        val trackDelayNoise = parameters.trackDelayNoise
        val trackDelay = parameters.trackDelay
        activeEvents = predicateResult?.events ?: emptyList()

        if (predicatePath == timedIdentifier) {
            return
        }
        else if (trackDelay != 0U && currentMusicPredicateId == predicatePath && !isPlaying(currentSoundInstance)) {
            val actualTrackDelay =
                max(0, (parameters.trackDelay.toInt() - trackDelayNoise.toInt()
                        ..parameters.trackDelay.toInt() + trackDelayNoise.toInt()).random()).toUInt()

            timedIdentifier = predicatePath
            timedIdentifierTimerTask = timedIdentifierTimer.schedule(actualTrackDelay.toLong() * 1000L) {
                timedIdentifier = ""
                currentMusicPredicateId = ""
            }

            return
        }
        else {
            timedIdentifierTimerTask?.cancel()
            timedIdentifier = ""
        }

        if (playingEvent != null && !playingEvent!!.parameters.isPersistent && !activeEvents.contains(playingEvent)) {
            playNow(null)
        }

        val nextMusic =
            if (jukeboxPlaying())
                null
            else
                predicateResult?.predicate?.playableSounds?.ifEmpty { listOf(null) }?.random()

        if (!shouldPlay(nextMusic, predicatePath))
        {
            return
        }

        if (predicatePath != currentMusicPredicateId
            && predicateResult?.events?.any { event -> event is OnEnterPredicateEvent } ?: false) {
            MusicEvent.invokeMusicEvent(TAMClient.eventRegistry[OnEnterPredicateEvent::class])
        }

        shouldResume = oldMusicPredicateId == predicatePath
        oldMusicPredicateId =
            if (predicatePath != currentMusicPredicateId)
                currentMusicPredicateId
            else
                oldMusicPredicateId
        currentMusicPredicateId = predicatePath
        startNewMusic(nextMusic)

        if (pauseDone) {
            currentSoundInstance
                ?.let {
                    volumeManager.startFade(
                        it, PAUSE_FADE_TICKS, PAUSE_VOLUME) }
        }
    }

    fun hasSoundInstance(instance: SoundInstance): Boolean {
        return currentSoundInstance === instance || oldSoundInstance === instance || onDemandSoundInstance === instance
    }

    private fun shouldPlay(music: PlayableSound?, predicatePath: String): Boolean {
        return (music == null || predicatePath != currentMusicPredicateId || !isPlaying(currentSoundInstance))
                && musicVolumeOption.get() > 0
    }

    private fun startNewMusic(newMusic: PlayableSound?) {
        if (newMusic == null)
        {
            if (isPlaying(currentSoundInstance)) {
                volumeManager.startFade(
                    currentSoundInstance!!,
                    REGULAR_FADE_TICKS,
                    0F,
                    true)
                currentSoundInstance = null
            }

            return
        }

        if (currentSoundInstance == null) {
            currentSoundInstance = newMusic.makeSoundInstance()
            playInstance(currentSoundInstance, keepBackground)
            if (!minecraft.soundManager.isActive(currentSoundInstance)) {
                currentSoundInstance = null
                currentMusicPredicateId = ""
            }

            return
        }

        if (shouldResume) {
            oldSoundInstance?.let { beginCrossfade(it) }
        }
        else {
            beginCrossfade(newMusic.makeSoundInstance())
        }
    }

    private fun playInstance(soundInstance: SoundInstance?, background: Boolean = false) {
        println("[TAM-DEBUG] MusicManager.playInstance called, instance: ${soundInstance?.javaClass?.name}")
        try {
            minecraft.soundManager.play(soundInstance)
            println("[TAM-DEBUG] SoundManager.play called successfully")
            if (background) {
                soundInstance?.let { volumeManager.setInstanceVolume(it, BACKGROUND_VOLUME) }
            }
        }
        catch (e: MusicLoadException) {
            Logger.logError("Error: Failed to play sound instance - ${e.message}")
        }
        catch (e: Exception) {
            println("[TAM-DEBUG] Exception in playInstance: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun stop() {
        volumeManager.clearFades()
        minecraft.soundManager.stop()
        currentSoundInstance = null
        oldSoundInstance = null
        onDemandSound = null
        onDemandSoundInstance = null
        timedIdentifierTimerTask?.cancel()
        timedIdentifier = ""
        timedIdentifierTimerTask = null
        currentMusicPredicateId = ""
        oldMusicPredicateId = ""
        activeEvents = emptyList()
    }

    private fun beginCrossfade(newSoundInstance: SoundInstance) {
        println("[TAM-DEBUG] beginCrossfade called with OpenAL fade support")
        
        // Fade out old sounds using OpenAL direct volume control
        oldSoundInstance?.let {
            println("[TAM-DEBUG] Fading out oldSoundInstance")
            volumeManager.startFade(it, REGULAR_FADE_TICKS, 0F, true)
        }
        
        currentSoundInstance?.let {
            println("[TAM-DEBUG] Fading out currentSoundInstance")
            volumeManager.startFade(it, REGULAR_FADE_TICKS, 0F, true)
        }

        oldSoundInstance = currentSoundInstance
        currentSoundInstance = newSoundInstance

        if (shouldResume) {
            if (keepBackground) {
                volumeManager.setInstanceVolume(currentSoundInstance!!, BACKGROUND_VOLUME)
            }
            minecraft.soundManager.resumeInstance(currentSoundInstance)
        }
        else {
            playInstance(currentSoundInstance, keepBackground)
        }
    }

    private fun isPlaying(soundInstance: SoundInstance?): Boolean {
        if (soundInstance == null) return false
        return minecraft.soundManager.isActive(soundInstance)
    }

    private fun jukeboxPlaying(): Boolean {
        // Simplified jukebox detection using public API
        // Check if any RECORDS source sound is playing nearby
        val player = minecraft.player ?: return false
        val playerPos = player.position()
        
        // We can't easily iterate all playing sounds without internal API access
        // This is a simplified check that may need refinement
        return false // For now, disable jukebox detection
    }

    companion object {
        private const val REGULAR_FADE_TICKS = 50
        private const val PLAY_NOW_FADE_TICKS = 10
        private const val PAUSE_FADE_TICKS = 5
        private const val BACKGROUND_VOLUME = 0.2F
        private const val PAUSE_VOLUME = 0.2F
    }
}