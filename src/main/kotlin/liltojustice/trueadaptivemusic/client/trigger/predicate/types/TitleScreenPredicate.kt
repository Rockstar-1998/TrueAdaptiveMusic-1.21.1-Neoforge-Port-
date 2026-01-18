package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import com.google.gson.JsonObject
import liltojustice.trueadaptivemusic.client.trigger.predicate.MusicPredicate
import net.minecraft.client.Minecraft

class TitleScreenPredicate(): MusicPredicate() {
    override fun test(minecraft: Minecraft): Boolean {
        return minecraft.level == null
    }

    companion object: MusicPredicateCompanion<TitleScreenPredicate> {
        override fun fromJson(json: JsonObject): TitleScreenPredicate {
            return TitleScreenPredicate()
        }
    }
}