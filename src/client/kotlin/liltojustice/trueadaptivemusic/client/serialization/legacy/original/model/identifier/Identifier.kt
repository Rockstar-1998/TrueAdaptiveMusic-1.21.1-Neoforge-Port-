package liltojustice.trueadaptivemusic.client.serialization.legacy.original.model.identifier

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import liltojustice.trueadaptivemusic.client.serialization.legacy.Convertible
import net.minecraft.resources.ResourceLocation

object Identifier: Convertible {
    override fun convert(json: JsonElement): JsonObject {
        val result = JsonObject()
        val id = Gson().toJsonTree(
            net.minecraft.resources.ResourceLocation.parse(json.asString),
            net.minecraft.resources.ResourceLocation::class.java
        )
        result.add("id", id)

        return result
    }
}
