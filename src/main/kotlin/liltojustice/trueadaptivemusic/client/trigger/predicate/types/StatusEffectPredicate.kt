package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import liltojustice.trueadaptivemusic.client.identifier.StatusEffectIdentifier
import liltojustice.trueadaptivemusic.client.trigger.predicate.MusicPredicate
import net.minecraft.client.Minecraft
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.util.GsonHelper

class StatusEffectPredicate(private val statusEffects: List<StatusEffectIdentifier>): MusicPredicate() {
    override fun test(minecraft: Minecraft): Boolean {
        val playerActiveEffects = minecraft.player?.activeEffects ?: return false

        return statusEffects.any { statusEffect ->
            playerActiveEffects.any { playerEffect ->
                BuiltInRegistries.MOB_EFFECT.getKey(playerEffect.effect.value()) == statusEffect.identifier } }
    }

    override fun toJson(): JsonObject {
        val result = JsonObject()
        val jsonEntities = JsonArray()
        statusEffects.forEach { statusEffect -> jsonEntities.add(statusEffect.toString()) }
        result.add("statusEffects", jsonEntities)

        return result
    }

    companion object: MusicPredicateCompanion<StatusEffectPredicate> {
        override fun fromJson(json: JsonObject): StatusEffectPredicate {
            return StatusEffectPredicate(
                json.getAsJsonArray("statusEffects")
                    .map { element -> StatusEffectIdentifier(element.asString) })
        }
    }
}