package liltojustice.trueadaptivemusic.client.gui.widget.utility

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.gui.narration.NarrationElementOutput
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.components.EditBox
import net.minecraft.client.gui.components.StringWidget
import net.minecraft.network.chat.Component

class TextInputWidget(
    private val screen: Screen,
    prompt: String,
    textFieldWidth: Int,
    onChange: (widget: TextInputWidget, text: String) -> Unit,
    placeholder: String = "",
    x: Int = 0,
    y: Int = 0)
    : AbstractWidget(x, y, 0, HEIGHT, Component.literal(prompt)) {
    private val font = Minecraft.getInstance().font
    private val promptWidget = StringWidget(Component.literal(prompt), font)
    private val fieldWidget = EditBox(font, 0, 0, textFieldWidth, HEIGHT, Component.literal(placeholder))
    var text: String
        get() { return fieldWidget.value }
        set(value) { fieldWidget.value = value }

    init {
        fieldWidget.setResponder { text -> onChange(this, text) }
        width = promptWidget.width + PADDING + fieldWidget.width
        text = placeholder
    }

    override fun renderWidget(context: GuiGraphics?, mouseX: Int, mouseY: Int, delta: Float) {
        promptWidget.x = x
        promptWidget.y = y
        fieldWidget.x = promptWidget.x + promptWidget.width + PADDING
        fieldWidget.y = y

        promptWidget.render(context, mouseX, mouseY, delta)
        fieldWidget.render(context, mouseX, mouseY, delta)
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        val clicked = fieldWidget.mouseClicked(mouseX, mouseY, button)
        if (clicked) {
            screen.focused = fieldWidget
        }

        return clicked
    }

    override fun updateWidgetNarration(narrationOutput: NarrationElementOutput) {
    }

    companion object {
        private const val HEIGHT = 10
        private const val PADDING = 5
    }
}