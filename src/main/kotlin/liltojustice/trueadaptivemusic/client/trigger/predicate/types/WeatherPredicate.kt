package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import com.google.gson.JsonObject
import liltojustice.trueadaptivemusic.client.trigger.predicate.MusicPredicate
import net.minecraft.client.Minecraft
import net.minecraft.util.GsonHelper

class WeatherPredicate(private val weather: Weather): MusicPredicate() {
    override fun test(minecraft: Minecraft): Boolean {
        val properties = minecraft.level?.getLevelData() ?: return false

        return when(weather) {
            Weather.Clear -> !properties.isRaining
            Weather.Rain -> properties.isRaining
            Weather.Thunder -> properties.isThundering
        }
    }

    override fun toJson(): JsonObject {
        val result = JsonObject()
        result.addProperty(FIELD_NAME, weather.name)

        return result
    }

    companion object: MusicPredicateCompanion<WeatherPredicate> {
        override fun fromJson(json: JsonObject): WeatherPredicate {
            return WeatherPredicate(Weather.valueOf(GsonHelper.getAsString(json, FIELD_NAME)))
        }

        private const val FIELD_NAME = "weatherType"
    }

    enum class Weather {
        Clear,
        Rain,
        Thunder
    }
}