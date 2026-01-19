package liltojustice.trueadaptivemusic.client.gui.screen

import liltojustice.trueadaptivemusic.client.TAMClient
import liltojustice.trueadaptivemusic.client.gui.widget.OptionsViewWidget
import net.neoforged.api.distmarker.Dist
import net.neoforged.api.distmarker.OnlyIn
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.gui.components.Button
import net.minecraft.network.chat.Component

@OnlyIn(Dist.CLIENT)
class OptionsScreen(private val parent: Screen): Screen(
    Component.literal("TrueAdaptiveMusic Options")) {
    private lateinit var optionsViewWidget: OptionsViewWidget
    private lateinit var doneButton: Button

    override fun init() {
        optionsViewWidget = OptionsViewWidget(
            TAMClient.options,
            width - BUFFER,
            height - BUFFER - TITLE_Y - font.lineHeight - 20,
            BUFFER / 2,
            BUFFER / 2 + TITLE_Y + font.lineHeight)

        doneButton = Button.builder(DONE_TEXT) { onClose() }
            .width(font.width(DONE_TEXT) + 10)
            .build()

        doneButton.x = width - doneButton.width
        doneButton.y = optionsViewWidget.y + optionsViewWidget.height + 2

        addRenderableWidget(optionsViewWidget)
        addRenderableWidget(doneButton)
    }

    override fun onClose() {
        TAMClient.options = optionsViewWidget.getCurrentOptions()
        minecraft?.setScreen(parent)
    }

    override fun render(context: GuiGraphics?, mouseX: Int, mouseY: Int, delta: Float) {
        renderBackground(context, mouseX, mouseY, delta)
        context?.drawCenteredString(
            this.font, this.title, this.width / 2, TITLE_Y, 16777215)
        super.render(context, mouseX, mouseY, delta)
    }

    companion object {
        private const val BUFFER = 6
        private const val TITLE_Y = 8
        private val DONE_TEXT = Component.literal("Done")
    }
}