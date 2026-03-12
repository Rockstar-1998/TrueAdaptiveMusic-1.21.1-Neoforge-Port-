package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import liltojustice.trueadaptivemusic.client.trigger.predicate.MusicPredicate
import net.minecraft.client.Minecraft

class NightTimePredicate: MusicPredicate() {
    override fun test(): Boolean {
        val client = Minecraft.getInstance()
        val world = client.level ?: return false
        val time = world.dayTime % 24000

        return time in 13000..23999
    }
}
