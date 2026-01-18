package liltojustice.trueadaptivemusic.client.gui.widget.utility

import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Font
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.narration.NarrationElementOutput
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.Mth
import kotlin.math.max

class CheckboxWidget(
    private val checkboxSize: Int,
    prompt: String,
    private val onChange: (checked: Boolean) -> Unit,
    x: Int = 0,
    y: Int = 0,
    checked: Boolean = true
) : AbstractWidget(x, y, 0, 0, Component.literal(prompt)) {
    
    private val font: Font = Minecraft.getInstance().font
    private var checked: Boolean = checked

    init {
        width = checkboxSize + PADDING + font.width(prompt)
        height = max(font.lineHeight, checkboxSize)
        onChange(this.checked)
    }

    fun isChecked(): Boolean = checked

    override fun onClick(mouseX: Double, mouseY: Double) {
        checked = !checked
        onChange(checked)
    }

    override fun renderWidget(context: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
        RenderSystem.enableDepthTest()

        context.setColor(1.0f, 1.0f, 1.0f, alpha)
        RenderSystem.enableBlend()
        context.blitSprite(
            if (checked) CHECKED else UNCHECKED,
            x,
            y,
            checkboxSize,
            checkboxSize,
        )
        context.setColor(1.0f, 1.0f, 1.0f, 1.0f)
        context.drawString(
            font,
            message,
            x + checkboxSize + PADDING,
            y,
            14737632 or (Mth.ceil(this.alpha * 255.0f) shl 24),
            true
        )
    }

    override fun updateWidgetNarration(narrationOutput: NarrationElementOutput) {
        // Empty narration implementation
    }

    companion object {
        private val UNCHECKED = ResourceLocation.parse("widget/checkbox")
        private val CHECKED = ResourceLocation.withDefaultNamespace("widget/checkbox_selected")
        private const val PADDING = 5
    }
}