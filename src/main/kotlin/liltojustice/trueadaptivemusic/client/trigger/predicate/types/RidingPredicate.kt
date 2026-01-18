package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import liltojustice.trueadaptivemusic.client.identifier.EntityTypeIdentifier
import liltojustice.trueadaptivemusic.client.trigger.predicate.MusicPredicate
import net.minecraft.client.Minecraft
import net.minecraft.util.GsonHelper

class RidingPredicate(private val entities: List<EntityTypeIdentifier>): MusicPredicate() {
    override fun test(minecraft: Minecraft): Boolean {
        val vehicleKey = minecraft.player?.vehicle?.type?.descriptionId ?: return false

        return entities.isEmpty() || entities.any { entity -> entity.toTranslationKey("entity") == vehicleKey }
    }

    override fun toJson(): JsonObject {
        val result = JsonObject()
        val jsonEntities = JsonArray()
        entities.forEach { entity -> jsonEntities.add(entity.toString()) }
        result.add("entities", jsonEntities)

        return result
    }

    companion object: MusicPredicateCompanion<RidingPredicate> {
        override fun fromJson(json: JsonObject): RidingPredicate {
            return RidingPredicate(
                json.getAsJsonArray("entities")
                    .map { element -> EntityTypeIdentifier(element.asString) })
        }
    }
}