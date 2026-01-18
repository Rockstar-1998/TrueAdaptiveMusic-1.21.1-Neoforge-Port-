package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import com.google.gson.JsonObject
import liltojustice.trueadaptivemusic.client.trigger.predicate.MusicPredicate
import net.minecraft.client.Minecraft

class PillagerRaidPredicate(): MusicPredicate() {
    override fun test(minecraft: Minecraft): Boolean {
        val clientWorld = minecraft.level ?: return false
        val server = minecraft.singleplayerServer ?: return false
        val serverWorld = server.allLevels.firstOrNull { world -> world.dimension() == clientWorld.dimension() }
            ?: return false

        return serverWorld.isRaided(minecraft.player?.blockPosition() ?: return false)
    }

    companion object: MusicPredicateCompanion<PillagerRaidPredicate> {
        override fun fromJson(json: JsonObject): PillagerRaidPredicate {
            return PillagerRaidPredicate()
        }
    }
}