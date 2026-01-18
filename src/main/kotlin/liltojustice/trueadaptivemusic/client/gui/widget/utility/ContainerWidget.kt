package liltojustice.trueadaptivemusic.client.gui.widget.utility

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Font
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.narration.NarrationElementOutput
import net.minecraft.network.chat.Component
import net.minecraft.util.CommonColors
import java.util.function.Consumer
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

abstract class ContainerWidget(
    width: Int,
    height: Int,
    message: String,
    private val showHeader: Boolean,
    private val bordered: Boolean,
    private val scrollable: Boolean = false,
    private val indentChildren: Boolean = true,
    x: Int = 0,
    y: Int = 0,
    private val translucentInteract: Boolean = false,
    backButtonCallback: (() -> Unit)? = null)
    : AbstractWidget(x, y, width, height, Component.literal(message)) {
    private val children = mutableMapOf<String, ChildWidget>()
    private val renderChildren = mutableMapOf<String, ChildWidget>()
    private val minecraft = Minecraft.getInstance()
    protected val font: Font = minecraft.font
    protected val screen = minecraft.screen
    private var scrollPosition = 0
    private var backButton = backButtonCallback?.let { makeBackButton(it) }

    fun addBackButton(backButtonCallback: (() -> Unit)) {
        backButton = makeBackButton(backButtonCallback)
    }

    override fun setHeight(height: Int) {
        this.height = height
    }

    override fun updateWidgetNarration(narrationOutput: NarrationElementOutput) {
        // Empty narration implementation
    }

    override fun renderWidget(context: GuiGraphics?, mouseX: Int, mouseY: Int, delta: Float) {
        renderChildren.clear()
        if (!visible) {
            return
        }

        //super.render(context, mouseX, mouseY, delta)

        if (showHeader)
        {
            context?.setColor(0f, 0f, 0f, if (bordered) 1f else 0.5f)
            context?.fill(x, y, x + this.width, y + this.height, CommonColors.BLACK)
            context?.fill(x, y,  x + width, y + TOP_MARGIN, CommonColors.BLACK)
            context?.setColor(1f, 1f, 1f, 1f)
            drawCenteredText(context, message.string, -1, width / 2, shadow = true)
            backButton?.let {
                it.x = x + 5
                it.y = (y + getHeaderOffset() - getRowHeight(font.lineHeight)).toInt()
                it.render(context, mouseX, mouseY, delta)
            }
            context?.setColor(1f, 1f, 1f, 1f)
        }

        if (bordered) {
            context?.fill(x, y, x + width, y + height, CommonColors.BLACK)
            context?.renderOutline(x, y, width, height, CommonColors.WHITE)
        }

        clampScrollPosition()
        drawScrollBar(context)

        context?.enableScissor(x, y + getHeaderOffset() - 2, x + width, y + height)
        children.forEach { (_, child) ->
            val translated = child.translated(scrollPosition)
            translated.widget.x = x + translated.xOffset + if (indentChildren) X_MARGIN else 0
            translated.widget.y = getTranslatedY(translated.row)
            translated.widget.width = min(translated.widget.width, width - translated.xOffset - 2 * X_MARGIN)
            val prevVisibility = translated.widget.visible
            translated.widget.visible = prevVisibility && contains(translated.widget)
            translated.widget.render(context, mouseX, mouseY, delta)
            translated.widget.visible = prevVisibility
        }
        context?.disableScissor()
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (!visible || !active) {
            return false
        }

        backButton?.let {
            if (it.isMouseOver(mouseX, mouseY)) {
                it.mouseClicked(mouseX, mouseY, button)
                return true
            }
        }

        // Copy to avoid concurrent modification
        val children = children.toList()
        children.forEach { (_, child) ->
            if (child.widget.isMouseOver(mouseX, mouseY)) {
                val clicked = child.widget.mouseClicked(mouseX, mouseY, button)
                if (clicked) {
                    screen?.focused = screen.focused ?: child.widget
                }
            }
        }

        return false
    }

    override fun mouseScrolled(mouseX: Double, mouseY: Double, horizontalAmount: Double, verticalAmount: Double): Boolean {
        if (!visible || !active) {
            return false
        }

        // Copy to avoid concurrent modification
        val children = children.toList()
        children.forEach { (_, child) ->
            if (child.widget.isMouseOver(mouseX, mouseY)) {
                child.widget.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)
                if (child.widget is ContainerWidget && child.widget.shouldBlockScroll(mouseX, mouseY))
                {
                    return@mouseScrolled isMouseOver(mouseX, mouseY)
                }
            }
        }

        if (!isMouseOver(mouseX, mouseY)) {
            return false
        }

        if (scrollable) {
            scrollPosition -= verticalAmount.toInt()
        }

        return true
    }

    protected fun drawString(
        GuiGraphics: GuiGraphics?,
        Component: String,
        row: Int,
        xOffset: Int = 0,
        color: Int = CommonColors.WHITE,
        shadow: Boolean = true) {
        GuiGraphics?.drawString(
            font,
            Component,
            X_MARGIN + xOffset + x,
            getTranslatedY(row),
            color,
            shadow)
    }

    protected fun drawCenteredText(
        GuiGraphics: GuiGraphics?,
        Component: String,
        row: Int,
        xOffset: Int = 0,
        color: Int = CommonColors.WHITE,
        shadow: Boolean = true) {
        GuiGraphics?.drawString(
            font,
            Component,
            xOffset + x - font.width(Component) / 2,
            getTranslatedY(row),
            color,
            shadow)
    }

    // Use if the widget is created on render
    fun addWidgetFromRender(
        widgetMaker: () -> AbstractWidget,
        widgetId: String,
        row: Int? = null,
        xOffset: Int = 0,
        shouldRecompute: () -> Boolean = { false }): AbstractWidget {
        if (!children.containsKey(widgetId) || shouldRecompute()) {
            children[widgetId] = ChildWidget(widgetId, widgetMaker(), row ?: 0, xOffset, true)
        }

        if (row == null) {
            children[widgetId] = children[widgetId]!!.copy(row = maxUsedRow(true, true) + 1)
        }

        renderChildren[widgetId] = children[widgetId]!!.copy()

        return children[widgetId]!!.widget
    }

    fun addWidget(child: AbstractWidget, row: Int, xOffset: Int = 0): AbstractWidget {
        val hash = child.hashCode().toString()
        if (!children.containsKey(hash)) {
            children[hash] = ChildWidget(hash, child, row, xOffset)
        }

        return children[hash]!!.widget
    }

    // Use to only clear widgets created from addWidgetToRender
    fun clearWidgetsFromRender(keepPredicate: (childWidget: ChildWidget) -> Boolean = { false }) {
        children
            .filterValues { child -> child.fromRender }
            .forEach { (key, child) ->
                if (!keepPredicate(child))
                    children.remove(key)
            }
        renderChildren.clear()
    }

    fun clearWidgets(keepPredicate: (childWidget: ChildWidget) -> Boolean = { false }) {
        // Copy to avoid concurrent modification
        val children = children.toList()
        children
            .forEach { (key, child) ->
                if (!keepPredicate(child))
                    this.children.remove(key)
            }
        renderChildren.clear()
    }

    fun fitToUsedRows(maxRows: Int = 0) {
        height = (
                (if (maxRows > 0)
                    min(maxRows, maxUsedRow(countOffscreen = true) + 1)
                else
                    maxUsedRow(countOffscreen = true) + 1)
                        * getRowHeight(font.lineHeight)
                        + getHeaderOffset()).toInt()
    }

    fun fitToChildrenHeight() {
        var max = 0
        children.filterValues { child -> childVisible(child.translated(scrollPosition)) }.forEach { (_, child) ->
            val translated = child.translated(scrollPosition)
            max = max(max, getTranslatedY(translated.row) - y + translated.widget.height)
        }
        height = (max + getRowHeight(font.lineHeight)).toInt()
    }

    fun fitToChildrenWidth() {
        var max = 0
        children.forEach { (_, child) ->
            val translated = child.translated(scrollPosition)
            if (childVisible(child)) {
                max = max(max, translated.widget.x + translated.xOffset + X_MARGIN + translated.widget.width - x)
            }
        }
        width = max
    }

    fun fitToChildren() {
        fitToChildrenHeight()
        fitToChildrenWidth()
    }

    fun forEachChild(action: (AbstractWidget) -> Unit) {
        children.values.forEach { child -> action(child.widget) }
    }

    fun resetScrolling() {
        scrollPosition = 0
    }

    fun childVisible(widget: AbstractWidget): Boolean {
        val childWidget = children.values.firstOrNull { child -> child.widget === widget }?.translated(scrollPosition)
        return childWidget?.let { childVisible(it) } == true
    }

    // Note: forEachChild was removed - not present in NeoForge AbstractWidget

    private fun clampScrollPosition() {
        scrollPosition = min(scrollPosition, maxUsedRow(countOffscreen = true) + 1 - totalRows())
        scrollPosition = max(0, scrollPosition)
    }

    private fun getHeaderOffset(): Int {
        return (if (showHeader) TOP_MARGIN else 0) + 2
    }

    private fun getTranslatedY(row: Int): Int {
        return (row * getRowHeight(font.lineHeight)).toInt() + getHeaderOffset() + y
    }

    private fun totalRows(): Int {
        return ((height - getHeaderOffset()) / getRowHeight(font.lineHeight)).roundToInt()
    }

    private fun maxUsedRow(onlyThisRender: Boolean = false, countOffscreen: Boolean = false): Int {
        return if (visible) (if (onlyThisRender) renderChildren else children)
            .mapValues { (_, child) -> if (countOffscreen) child else child.translated(scrollPosition) }
            .filterValues { child ->
                if (countOffscreen) child.widget.visible else childVisible(child) }
            .maxOfOrNull { (_, child) ->
                child.row + if (child.widget is ContainerWidget) child.widget.maxUsedRow() + 1 else 0 }
            ?: 0 else 0
    }

    private fun drawScrollBar(context: GuiGraphics?) {
        if (!scrollable) {
            return
        }

        val usedRows = maxUsedRow(countOffscreen = true) + 1
        val totalRows = totalRows()
        if (usedRows > totalRows) {
            val adjustedHeight = height - getHeaderOffset() - 2
            val ratio = totalRows.toDouble() / usedRows
            val barSize = ratio * adjustedHeight
            val start = (scrollPosition.toDouble() / (usedRows - totalRows)) * adjustedHeight * (1 - ratio)
            val end = start + barSize
            val y1 = (y + start + getHeaderOffset()).toInt()
            val y2 = (y + end + getHeaderOffset()).toInt()
            val diff = y2 - y1
            context?.vLine(
                x + width - 3,
                y1,
                if (diff < 2) y2 + (2 - diff) else y2,
                CommonColors.WHITE
            )
        }
    }

    private fun childVisible(translated: ChildWidget): Boolean {
        return translated.widget.visible && translated.row >= 0 && translated.row < totalRows()
    }

    private fun shouldBlockScroll(mouseX: Double, mouseY: Double): Boolean {
        return (!translucentInteract && visible && active && isMouseOver(mouseX, mouseY))
                || children.any { (_, child) ->
            child.widget is ContainerWidget && child.widget.shouldBlockScroll(mouseX, mouseY) }
    }

    private fun contains(widget: AbstractWidget): Boolean {
        val left = x
        val right = left + width
        val top = y + getHeaderOffset()
        val bottom = top + height - getHeaderOffset()
        val widgetLeft = widget.x
        val widgetRight = widgetLeft + widget.width
        val widgetTop = widget.y
        val widgetBottom = widgetTop + widget.height
        return left <= widgetRight && right >= widgetLeft && top <= widgetBottom && bottom >= widgetTop
    }

    companion object {
        private const val TOP_MARGIN = 12
        private const val X_MARGIN = 5

        fun getRowHeight(fontHeight: Int): Double {
            return (1.35 * fontHeight)
        }

        private fun makeBackButton(backButtonCallback: () -> Unit): ClickableTextWidget {
            return backButtonCallback.let { ClickableTextWidget("Back", onClick = { it() }) }
        }
    }

    data class ChildWidget(
        val id: String, val widget: AbstractWidget, val row: Int, val xOffset: Int, val fromRender: Boolean = false) {
        fun translated(row: Int, xOffset: Int = 0): ChildWidget {
            return copy(row = this.row - row, xOffset = this.xOffset + xOffset)
        }
    }
}