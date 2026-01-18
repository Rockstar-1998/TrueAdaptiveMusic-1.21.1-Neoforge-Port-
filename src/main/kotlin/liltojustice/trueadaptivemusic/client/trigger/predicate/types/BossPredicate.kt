package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import liltojustice.trueadaptivemusic.client.identifier.EntityTypeIdentifier
import liltojustice.trueadaptivemusic.client.javasucks.BossEventHelper
import liltojustice.trueadaptivemusic.client.trigger.predicate.MusicPredicate
import net.minecraft.client.Minecraft
import net.minecraft.network.chat.contents.TranslatableContents
import net.minecraft.util.GsonHelper

class BossPredicate(private val bosses: List<EntityTypeIdentifier>): MusicPredicate() {
    override fun test(minecraft: Minecraft): Boolean {
        return BossEventHelper.getEvents(minecraft.gui.bossOverlay).values.any { bossBar ->
            val bossName = (bossBar.name.contents as? TranslatableContents)?.key ?: return@any false
            bosses.isEmpty() || bosses.any { boss -> bossName == boss.toTranslationKey("entity") }
        }
    }

    override fun toJson(): JsonObject {
        val result = JsonObject()
        val jsonBosses = JsonArray()
        bosses.forEach { boss -> jsonBosses.add(boss.toString()) }
        result.add("id", jsonBosses)

        return result
    }

    companion object: MusicPredicateCompanion<BossPredicate> {
        override fun fromJson(json: JsonObject): BossPredicate {
            return BossPredicate(
                if (json.has("id") && json.get("id").isJsonArray)
                    json.getAsJsonArray("id").map { element -> EntityTypeIdentifier(element.asString) }
                else
                    listOf(EntityTypeIdentifier(GsonHelper.getAsString(json, "id"))))
        }
    }
}