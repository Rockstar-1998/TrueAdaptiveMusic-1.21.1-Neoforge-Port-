package liltojustice.trueadaptivemusic.client.javasucks

import liltojustice.trueadaptivemusic.client.TAMClient
import liltojustice.trueadaptivemusic.client.identifier.EntityTypeIdentifier
import liltojustice.trueadaptivemusic.client.trigger.event.MusicEvent
import liltojustice.trueadaptivemusic.client.trigger.event.types.OnBossDefeatEvent
import net.minecraft.client.Minecraft
import net.minecraft.world.entity.LivingEntity
import net.minecraft.network.chat.contents.TranslatableContents

object OnBossDefeatEventMixinHelper {
    @JvmStatic
    fun onDeath(entity: LivingEntity) {
        if (isBoss(entity))
        {
            MusicEvent.invokeMusicEvent(
                TAMClient.eventRegistry[OnBossDefeatEvent::class],
                EntityTypeIdentifier(entity.type.descriptionId)
            )
        }
    }

    private fun isBoss(entity: LivingEntity): Boolean {
        val minecraft = Minecraft.getInstance()
        return BossEventHelper.getEvents(minecraft.gui.bossOverlay).values.any { bossBar ->
            val bossName = (bossBar.name.contents as? TranslatableContents)?.key ?: return@any false
            bossName == entity.type.descriptionId
        }
    }
}