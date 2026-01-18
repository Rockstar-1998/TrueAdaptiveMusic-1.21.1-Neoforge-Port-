package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import com.google.gson.JsonObject
import liltojustice.trueadaptivemusic.client.trigger.predicate.MusicPredicate
import net.minecraft.client.Minecraft
import net.minecraft.util.GsonHelper
import net.minecraft.world.level.GameType

class GameModePredicate(private val gameMode: GameType): MusicPredicate() {
    override fun test(minecraft: Minecraft): Boolean {
        val currentGameMode = minecraft.connection?.getPlayerInfo(minecraft.player?.uuid ?: return false)?.gameMode

        return currentGameMode == gameMode
    }

    override fun toJson(): JsonObject {
        val result = JsonObject()
        result.addProperty(FIELD_NAME, gameMode.getName())

        return result
    }

    companion object: MusicPredicateCompanion<GameModePredicate> {
        override fun fromJson(json: JsonObject): GameModePredicate {
            return GameModePredicate(GameType.byName(GsonHelper.getAsString(json, FIELD_NAME)))
        }

        private const val FIELD_NAME = "gameMode"
    }
}