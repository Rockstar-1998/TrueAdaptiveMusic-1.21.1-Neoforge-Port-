package liltojustice.trueadaptivemusic.client.gui.widget.utility

import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.narration.NarrationElementOutput
import net.minecraft.client.gui.components.Tooltip
import net.minecraft.client.gui.components.EditBox
import net.minecraft.network.chat.Component
import kotlin.math.max

class DropdownWidget<TKey>(
    options: List<TKey>,
    onSelectOption: (optionKey: TKey) -> Unit,
    width: Int = 0,
    title: String = "",
    getDisplay: ((TKey) -> String)? = null,
    getOptions: (() -> List<TKey>)? = null,
    notSelectedPlaceholder: String? = null,
    startingOption: TKey? = null,
    private val onHoverOption: (option: String?) -> Unit = {},
    tooltipText: Component? = null,
    x: Int = 0,
    y: Int = 0
)
    : ContainerWidget(
    width,
    0,
    "Dropdown: $title",
    false,
    false,
    false,
    false,
    false,
    x,
    y,
    true) {
    private val titleText = Component.literal(title)
    private var dropdownResultsWidget: DropdownResultsWidget<TKey>
    private val realizedWidth = width.takeUnless { width == 0 }
        ?: (
                max(
                    textRenderer.width(title),
                    (options + (getOptions?.invoke() ?: listOf()))
                        .map { getDisplay?.invoke(it) ?: it.toString() }
                        .maxOfOrNull { option -> textRenderer.width(option) } ?: 0
                ) + TEXT_WIDTH_BUFFER)
    private val textInputWidget = EditBox(
        textRenderer,
        0,
        0,
        realizedWidth,
        textRenderer.lineHeight + TEXT_HEIGHT_BUFFER,
        Component.literal("Dropdown Search")
    )
    private val selectedOptionWidget = run {
        val combinedOptions = options + (getOptions?.invoke() ?: listOf())
        ClickableTextDisplayWidget(
            notSelectedPlaceholder
                ?: (combinedOptions.firstOrNull { it == startingOption } ?: combinedOptions.firstOrNull())
                    ?.let { option -> getDisplay?.invoke(option) ?: option.toString() } ?: ""
        )
    }
    private val titleTextWidget = ClickableTextWidget(titleText.string)

    init {
        titleTextWidget.disableBold()
        tooltipText?.let { setTooltip(Tooltip.create(it)) }
        this.width = realizedWidth
        dropdownResultsWidget = DropdownResultsWidget(
            options,
            { option ->
                selectedOptionWidget.setText(getDisplay?.invoke(option) ?: option.toString())
                onSelectOption(option)
            },
            getDisplay,
            getOptions,
            notSelectedPlaceholder,
            startingOption,
            onHoverOption,
            x,
            y)
        textInputWidget.setResponder { newText ->
            dropdownResultsWidget.setSearchText(newText)
        }
        addWidget(titleTextWidget, 0)
        addWidget(selectedOptionWidget, 1)
        addWidget(textInputWidget, 1)
        addWidget(dropdownResultsWidget, 2)
        close()
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        val result = super.mouseClicked(mouseX, mouseY, button)
        textInputWidget.value = ""
        if (focusedWidget == selectedOptionWidget) {
            open()
        }
        else if (focusedWidget != dropdownResultsWidget || dropdownResultsWidget.focusedWidget != null || !result) {
            close()
        }

        return result
    }

    override fun renderWidget(context: GuiGraphics?, mouseX: Int, mouseY: Int, delta: Float) {
        super.renderWidget(context, mouseX, mouseY, delta)
        fitToChildrenHeight()
    }

    override fun updateWidgetNarration(builder: NarrationElementOutput) {
    }

    private fun open() {
        focusedWidget = textInputWidget
        onHoverOption(null)
        textInputWidget.visible = true
        textInputWidget.isFocused = true
        selectedOptionWidget.visible = false
        dropdownResultsWidget.visible = true
        dropdownResultsWidget.width = width
    }

    private fun close() {
        onHoverOption(null)
        textInputWidget.visible = false
        textInputWidget.isFocused = false
        selectedOptionWidget.visible = true
        dropdownResultsWidget.visible = false
        dropdownResultsWidget.width = width
    }

    companion object {
        const val TEXT_WIDTH_BUFFER = 25
        const val TEXT_HEIGHT_BUFFER = 5
    }

    private class DropdownResultsWidget<TKey>(
        private val options: List<TKey>,
        val onSelectOption: (optionKey: TKey) -> Unit,
        private val getDisplay: ((TKey) -> String)?,
        private val getOptions: (() -> List<TKey>)?,
        notSelectedPlaceholder: String?,
        startingOption: TKey?,
        private val onHoverOption: (option: String?) -> Unit,
        x: Int = 0,
        y: Int = 0)
        : ContainerWidget(
        0,
        0,
        "Dropdown List",
        false,
        true,
        true,
        false,
        true,
        x,
        y) {
        private var selectedOption = run {
            val combinedOptions = options + (getOptions?.invoke() ?: listOf())
            startingOption ?: combinedOptions.firstOrNull()
        }
        private var searchText = ""

        init {
            if (notSelectedPlaceholder == null) {
                selectedOption?.let { onSelectOption(it) }
            }
        }

        override fun renderWidget(context: GuiGraphics?, mouseX: Int, mouseY: Int, delta: Float) {
            if (!visible) {
                return
            }

            ((getOptions?.invoke() ?: listOf()) + options)
                .map { it to (getDisplay?.invoke(it) ?: it.toString()) }
                .filter { option -> option.second.lowercase().contains(searchText.lowercase()) }
                .mapIndexed { index, option ->
                    addWidgetFromRender(
                        {
                            ClickableTextWidget(
                                option.second,
                                onClick = {
                                    selectedOption = option.first
                                    onSelectOption(option.first)
                                },
                                onMouseOn = { option -> onHoverOption(option.text) },
                                onMouseOff = { option -> onHoverOption(null) })
                        },
                        option.first.hashCode().toString(),
                        index
                    )
                }

            fitToUsedRows(MAX_DISPLAYED_OPTIONS)
            super.renderWidget(context, mouseX, mouseY, delta)
        }

        fun setSearchText(searchText: String) {
            this.searchText = searchText
            clearWidgetsFromRender()
        }

        override fun updateWidgetNarration(builder: NarrationElementOutput) {
        }

        companion object {
            const val MAX_DISPLAYED_OPTIONS = 5
        }
    }
}


