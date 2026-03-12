package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import liltojustice.trueadaptivemusic.client.trigger.predicate.MusicPredicate
import net.minecraft.client.Minecraft

class DayTimePredicate: MusicPredicate() {
    override fun test(): Boolean {
        val client = Minecraft.getInstance()
        val world = client.level ?: return false
        val time = world.dayTime % 24000

        return time in 0..12999
    }

    override fun getTickRate(): Int {
        return super.getTickRate() * 2
    }
}
