package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import liltojustice.trueadaptivemusic.client.trigger.predicate.MusicPredicate
import net.minecraft.client.Minecraft

class InWaterPredicate: MusicPredicate() {
    private var tickBuffer = 0

    override fun test(): Boolean {
        val client = Minecraft.getInstance()
        val result = client.player?.isInWater ?: false
        if (result) {
            tickBuffer = GRACE_PERIOD_TICKS
        }
        else if (tickBuffer-- > 0) {
            return true
        }

        return result
    }

    companion object {
        private const val GRACE_PERIOD_TICKS = 6
    }
}
