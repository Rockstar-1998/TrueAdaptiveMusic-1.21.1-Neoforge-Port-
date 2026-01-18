package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import com.google.gson.JsonObject
import liltojustice.trueadaptivemusic.client.trigger.predicate.MusicPredicate
import net.minecraft.client.Minecraft
import net.minecraft.util.GsonHelper

class HeightPredicate(private val above: Boolean, private val y: Int): MusicPredicate() {
    override fun test(minecraft: Minecraft): Boolean {
        val playerHeight = minecraft.player?.blockPosition()?.y ?: return false

        return if (above) playerHeight >= y else playerHeight <= y
    }

    override fun toJson(): JsonObject {
        val result = JsonObject()
        result.addProperty("above", above)
        result.addProperty("y", y)

        return result
    }

    companion object: MusicPredicateCompanion<HeightPredicate> {
        override fun fromJson(json: JsonObject): HeightPredicate {
            return HeightPredicate(GsonHelper.getAsBoolean(json, "above"), GsonHelper.getAsInt(json, "y"))
        }
    }
}