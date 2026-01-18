package liltojustice.trueadaptivemusic.client.trigger.event.types

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import liltojustice.trueadaptivemusic.client.trigger.event.MusicEvent
import liltojustice.trueadaptivemusic.client.identifier.EntityTypeIdentifier
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.GsonHelper

class OnBossDefeatEvent(private val bosses: List<EntityTypeIdentifier>): MusicEvent() {
    override fun validate(vararg eventArgs: Any?): Boolean {
        val bossId = ResourceLocation.tryParse((eventArgs[0] as? ResourceLocation)
            ?.path?.split(".")?.drop(1)?.joinToString(":")) ?: return false
        return bosses.isEmpty()
                || bosses.any {
                    bossId.namespace == it.identifier.namespace && bossId.path.split(".").lastOrNull() == it.identifier.path }
    }

    override fun toJson(): JsonObject {
        val result = JsonObject()
        val bossesArray = JsonArray()
        bosses.forEach { bossesArray.add(it.toString()) }
        result.add("bosses", bossesArray)

        return result
    }

    companion object: MusicEventCompanion<OnBossDefeatEvent> {
        override fun fromJson(json: JsonObject): MusicEvent {
            return OnBossDefeatEvent(
                if (json.has("bosses") && json.get("bosses").isJsonArray)
                    json.getAsJsonArray("bosses").map { element -> EntityTypeIdentifier(element.asString) }
                else
                    listOf())
        }
    }
}