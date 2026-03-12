package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import liltojustice.trueadaptivemusic.client.identifier.EntityTypeIdentifier
import liltojustice.trueadaptivemusic.client.trigger.predicate.MusicPredicate
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.components.BossHealthOverlay
import net.minecraft.client.gui.components.LerpingBossEvent
import net.minecraft.network.chat.contents.TranslatableContents

class BossPredicate(private val bosses: List<EntityTypeIdentifier>): MusicPredicate() {
    override fun test(): Boolean {
        val client = Minecraft.getInstance()
        return getBossEvents(client).any { bossBar ->
            val bossName = (bossBar.name.contents as? TranslatableContents)?.key ?: return@any false
            bosses.isEmpty() || bosses.any { boss -> bossName == boss.toTranslationKey("entity") }
        }
    }

    companion object: MusicPredicateCompanion {
        override val argDescriptions: Map<String, String>
            get() = super.argDescriptions + mapOf(
                "bosses" to "List of entities that the music should play for. If none, any entity will trigger the " +
                        "music."
            )

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
}
