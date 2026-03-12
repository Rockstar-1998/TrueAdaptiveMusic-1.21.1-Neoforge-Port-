package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import liltojustice.trueadaptivemusic.client.trigger.predicate.MusicPredicate
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.components.BossHealthOverlay
import net.minecraft.client.gui.components.LerpingBossEvent

class BossHealthPredicate(private val direction: Direction, private val healthPercentage: Int): MusicPredicate() {
    override fun test(): Boolean {
        val client = Minecraft.getInstance()
        return getBossEvents(client).any { bossBar ->
            healthTest((healthPercentage / 100F), direction, bossBar.progress) }
    }

    override fun getTickRate(): Int {
        return super.getTickRate() * 4
    }

    companion object: MusicPredicateCompanion {
        override val argDescriptions: Map<String, String>
            get() = super.argDescriptions + mapOf(
                "direction" to "Whether the music should play above or below the given health percentage.",
                "healthPercentage" to "The threshold at which the predicate switches."
            )

        private fun healthTest(thresholdPercentage: Float, direction: Direction, currentPercentage: Float): Boolean {
            return when (direction) {
                Direction.Greater -> currentPercentage > thresholdPercentage
                Direction.Lesser -> currentPercentage < thresholdPercentage
            }
        }

        private fun getBossEvents(client: Minecraft): Collection<LerpingBossEvent> {
            val bossOverlay = client.gui.bossOverlay
            return runCatching {
                val field = BossHealthOverlay::class.java.getDeclaredField("events")
                field.isAccessible = true
                val map = field.get(bossOverlay) as? Map<*, *> ?: return emptyList()
                map.values.filterIsInstance<LerpingBossEvent>()
            }.getOrDefault(emptyList())
        }
    }

    enum class Direction {
        Greater,
        Lesser
    }
}
