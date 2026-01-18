package liltojustice.trueadaptivemusic.client.gui.widget.utility

import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.narration.NarrationElementOutput
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.network.chat.Component

class EmptyClickableWidget(): AbstractWidget(0, 0, 0, 0, Component.literal("")) {
    override fun renderWidget(
        context: GuiGraphics?,
        mouseX: Int,
        mouseY: Int,
        delta: Float
    ) {
    }

    override fun updateWidgetNarration(narrationOutput: NarrationElementOutput) {
    }
}