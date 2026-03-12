package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import liltojustice.trueadaptivemusic.client.trigger.predicate.MusicPredicate
import net.minecraft.client.Minecraft

class PausedPredicate: MusicPredicate() {
    override fun test(): Boolean {
        val client = Minecraft.getInstance()
        return client.level != null && (client.screen?.isPauseScreen() ?: false)
    }
}
