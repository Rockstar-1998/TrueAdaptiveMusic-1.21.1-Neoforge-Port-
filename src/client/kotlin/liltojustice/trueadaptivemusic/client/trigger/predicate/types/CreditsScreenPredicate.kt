package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import liltojustice.trueadaptivemusic.client.trigger.predicate.MusicPredicate
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.WinScreen

class CreditsScreenPredicate: MusicPredicate() {
    override fun test(): Boolean {
        val client = Minecraft.getInstance()
        return client.screen is WinScreen
    }
}
