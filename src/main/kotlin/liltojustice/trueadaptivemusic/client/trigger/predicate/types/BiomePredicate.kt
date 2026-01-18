package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import liltojustice.trueadaptivemusic.client.identifier.BiomeIdentifier
import liltojustice.trueadaptivemusic.client.trigger.predicate.MusicPredicate
import net.minecraft.client.Minecraft
import net.minecraft.util.GsonHelper

class BiomePredicate(private val biomes: List<BiomeIdentifier>): MusicPredicate() {
    override fun test(minecraft: Minecraft): Boolean {
        val player = minecraft.player ?: return false
        val playerBiome = minecraft.level?.getBiome(player.blockPosition()) ?: return false

        return biomes.isEmpty() || biomes.any { biome -> playerBiome.`is`(biome.identifier) }
    }

    override fun toJson(): JsonObject {
        val result = JsonObject()
        val jsonBiomes = JsonArray()
        biomes.forEach { biome -> jsonBiomes.add(biome.toString()) }
        result.add("id", jsonBiomes)

        return result
    }

    companion object: MusicPredicateCompanion<BiomePredicate> {
        override fun fromJson(json: JsonObject): BiomePredicate {
            return BiomePredicate(
                    if (json.has("id") && json.get("id").isJsonArray)
                        json.getAsJsonArray("id").map { element -> BiomeIdentifier(element.asString) }
                    else
                        listOf(BiomeIdentifier(GsonHelper.getAsString(json, "id"))))
        }
    }
}