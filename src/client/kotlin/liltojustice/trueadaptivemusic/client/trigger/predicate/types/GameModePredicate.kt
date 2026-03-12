package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import liltojustice.trueadaptivemusic.client.trigger.predicate.MusicPredicate
import net.minecraft.client.Minecraft
import net.minecraft.world.level.GameType

class GameModePredicate(private val gameMode: GameType): MusicPredicate() {
    override fun test(): Boolean {
        val client = Minecraft.getInstance()
        val currentGameMode =
            client.connection?.getPlayerInfo(client.player?.uuid ?: return false)?.gameMode

        return currentGameMode == gameMode
    }

    companion object: MusicPredicateCompanion {
        override val argDescriptions: Map<String, String>
            get() = super.argDescriptions + mapOf(
                "gameMode" to "Which game mode to be in for the music to play."
            )
    }
}
