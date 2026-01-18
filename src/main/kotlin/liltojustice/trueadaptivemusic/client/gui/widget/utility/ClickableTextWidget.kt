package liltojustice.trueadaptivemusic.client.gui.widget.utility

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.narration.NarrationElementOutput
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.network.chat.Component
import net.minecraft.util.CommonColors

open class ClickableTextWidget(
    text: String,
    x: Int = 0,
    y: Int = 0,
    private val showHighlight: Boolean = true,
    private val onClick: (ClickableTextWidget) -> Unit = {},
    private val isSelected: (ClickableTextWidget) -> Boolean = { false })
    : AbstractWidget(x, y, 0, 0, Component.literal(text)),
    DataWrapped<ClickableTextWidget> {
    override var customData: Any? = null
    private val font = Minecraft.getInstance().font
    var color: Int = CommonColors.WHITE
    val text: String
        get() = message.string

    init {
        width = font.width(message)
        height = font.lineHeight
    }

    override fun renderWidget(context: GuiGraphics?, mouseX: Int, mouseY: Int, delta: Float) {
        if (!visible) {
            return
        }

        val selected = isSelected(this)
        if (selected) {
            context?.renderOutline(
                x - BORDER_BUFFER / 2,
                y - BORDER_BUFFER / 2,
                width + BORDER_BUFFER,
                height + BORDER_BUFFER,
                CommonColors.WHITE
            )
        }

        if (!selected && showHighlight && isMouseOver(mouseX.toDouble(), mouseY.toDouble())) {
            context?.hLine(x, x + width, y + font.lineHeight, CommonColors.WHITE)
        }

        context?.drawString(font, message, x, y, color, true)
    }

    override fun onClick(mouseX: Double, mouseY: Double) {
        super.onClick(mouseX, mouseY)

        if (visible && active)
        {
            onClick(this)
        }
    }

    override fun updateWidgetNarration(narrationOutput: NarrationElementOutput) {
    }

    fun setText(text: String) {
        message = Component.literal(text)
        this.width = font.width(message)
    }

    companion object {
        const val BORDER_BUFFER = 4
    }
}