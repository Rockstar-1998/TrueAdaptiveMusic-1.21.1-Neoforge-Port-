package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import liltojustice.trueadaptivemusic.client.identifier.DimensionIdentifier
import liltojustice.trueadaptivemusic.client.trigger.predicate.MusicPredicate
import net.minecraft.client.Minecraft
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.GsonHelper

class DimensionPredicate(private val dimensions: List<DimensionIdentifier>): MusicPredicate() {
    override fun test(minecraft: Minecraft): Boolean {
        val level = minecraft.level ?: return false
        val playerDimensionKey = level.dimension().location()

        return dimensions.isEmpty() || dimensions.any { dimension -> playerDimensionKey == dimension.identifier }
    }

    override fun toJson(): JsonObject {
        val result = JsonObject()
        val jsonDimensions = JsonArray()
        dimensions.forEach { dimension -> jsonDimensions.add(dimension.toString()) }
        result.add("id", jsonDimensions)

        return result
    }

    companion object: MusicPredicateCompanion<DimensionPredicate> {
        override fun fromJson(json: JsonObject): DimensionPredicate {
            return DimensionPredicate(
                if (json.has("id") && json.get("id").isJsonArray)
                    json.getAsJsonArray("id").map { element -> DimensionIdentifier(element.asString) }
                else
                    listOf(DimensionIdentifier(GsonHelper.getAsString(json, "id"))))
        }
    }
}