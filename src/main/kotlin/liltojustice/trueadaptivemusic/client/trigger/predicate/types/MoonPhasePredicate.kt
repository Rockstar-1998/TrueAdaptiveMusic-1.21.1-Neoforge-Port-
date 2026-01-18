package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import com.google.gson.JsonObject
import liltojustice.trueadaptivemusic.client.trigger.predicate.MusicPredicate
import net.minecraft.client.Minecraft
import net.minecraft.util.GsonHelper
import kotlin.ranges.contains

class MoonPhasePredicate(private val moonPhase: MoonPhase): MusicPredicate() {
    override fun test(minecraft: Minecraft): Boolean {
        val world = minecraft.level ?: return false
        val currentPhase = world.moonPhase
        val time = world.getDayTime() % 24000

        return time in 13000..23999 && when(moonPhase) {
            MoonPhase.Full -> currentPhase == 0
            MoonPhase.New -> currentPhase == 4
        }
    }

    override fun toJson(): JsonObject {
        val result = JsonObject()
        result.addProperty(FIELD_NAME, moonPhase.name)

        return result
    }

    companion object: MusicPredicateCompanion<MoonPhasePredicate> {
        override fun fromJson(json: JsonObject): MoonPhasePredicate {
            return MoonPhasePredicate(MoonPhase.valueOf(GsonHelper.getAsString(json, FIELD_NAME)))
        }

        private const val FIELD_NAME = "moonPhase"
    }

    enum class MoonPhase {
        New,
        Full
    }
}