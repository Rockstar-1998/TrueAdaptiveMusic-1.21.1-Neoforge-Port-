package liltojustice.trueadaptivemusic.client.javasucks

import liltojustice.trueadaptivemusic.client.TAMClient
import liltojustice.trueadaptivemusic.client.identifier.EntityTypeIdentifier
import liltojustice.trueadaptivemusic.client.trigger.event.types.OnBossDefeatEvent
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.components.BossHealthOverlay
import net.minecraft.client.gui.components.LerpingBossEvent
import net.minecraft.world.entity.LivingEntity
import net.minecraft.network.chat.contents.TranslatableContents
import net.minecraft.resources.ResourceLocation

object OnBossDefeatEventMixinHelper {
    @JvmStatic
    fun onDeath(entity: LivingEntity) {
        if (!isBoss(entity)) {
            return
        }

        TAMClient.invokeMusicEvent(
            OnBossDefeatEvent::class,
            EntityTypeIdentifier(ResourceLocation.tryParse(entity.type.toString()) ?: return)
        )
    }

    private fun isBoss(entity: LivingEntity): Boolean {
        val client = Minecraft.getInstance()
        return getBossEvents(client).any { bossBar ->
            val bossName = (bossBar.name.contents as? TranslatableContents)?.key ?: return@any false
            bossName == entity.type.descriptionId
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
