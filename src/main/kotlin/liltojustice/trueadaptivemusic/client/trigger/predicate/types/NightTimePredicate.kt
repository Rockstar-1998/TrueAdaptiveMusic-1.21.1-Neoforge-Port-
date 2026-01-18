package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import com.google.gson.JsonObject
import liltojustice.trueadaptivemusic.client.trigger.predicate.MusicPredicate
import net.minecraft.client.Minecraft

class NightTimePredicate: MusicPredicate() {
    override fun test(minecraft: Minecraft): Boolean {
        val world = minecraft.level ?: return false
        val time = world.getDayTime() % 24000

        return time in 13000..23999
    }

    override fun getTickRate(): Int {
        return 100
    }

    companion object: MusicPredicateCompanion<NightTimePredicate>
    {
        override fun fromJson(json: JsonObject): NightTimePredicate {
            return NightTimePredicate()
        }
    }
}