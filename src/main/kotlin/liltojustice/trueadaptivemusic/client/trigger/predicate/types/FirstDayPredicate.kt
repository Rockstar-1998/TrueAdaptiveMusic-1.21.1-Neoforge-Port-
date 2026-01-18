package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import com.google.gson.JsonObject
import liltojustice.trueadaptivemusic.client.trigger.predicate.MusicPredicate
import net.minecraft.client.Minecraft

class FirstDayPredicate(): MusicPredicate() {
    override fun test(minecraft: Minecraft): Boolean {
        val time = minecraft.level?.getGameTime() ?: return false

        return time <= 24000L
    }

    companion object: MusicPredicateCompanion<FirstDayPredicate> {
        override fun fromJson(json: JsonObject): FirstDayPredicate {
            return FirstDayPredicate()
        }
    }
}