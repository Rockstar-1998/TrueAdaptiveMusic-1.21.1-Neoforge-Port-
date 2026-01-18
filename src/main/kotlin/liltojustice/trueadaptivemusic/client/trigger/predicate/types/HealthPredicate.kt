package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import com.google.gson.JsonObject
import liltojustice.trueadaptivemusic.client.trigger.predicate.MusicPredicate
import net.minecraft.client.Minecraft
import net.minecraft.util.GsonHelper

class HealthPredicate(private val healthType: HealthType, private val direction: Direction, private val health: Int): MusicPredicate() {
    override fun test(minecraft: Minecraft): Boolean {
        val player = minecraft.player ?: return false
        val typeAdjusted = if (healthType == HealthType.Percentage) player.maxHealth * (health / 100F) else health.toFloat()

        return when (direction) {
            Direction.Greater -> player.health > typeAdjusted
            Direction.GreaterOrEqual -> player.health >= typeAdjusted
            Direction.Lesser -> player.health < typeAdjusted
            Direction.LesserOrEqual -> player.health <= typeAdjusted
        }
    }

    override fun toJson(): JsonObject {
        val result = JsonObject()
        result.addProperty("healthType", healthType.name)
        result.addProperty("direction", direction.name)
        result.addProperty("health", health)

        return result
    }

    companion object: MusicPredicateCompanion<HealthPredicate> {
        override fun fromJson(json: JsonObject): HealthPredicate {
            return HealthPredicate(
                HealthType.valueOf(GsonHelper.getAsString(json, "healthType")),
                Direction.valueOf(GsonHelper.getAsString(json, "direction")),
                GsonHelper.getAsInt(json, "health"))
        }
    }

    enum class HealthType {
        Value,
        Percentage
    }

    enum class Direction {
        Greater,
        GreaterOrEqual,
        Lesser,
        LesserOrEqual
    }
}