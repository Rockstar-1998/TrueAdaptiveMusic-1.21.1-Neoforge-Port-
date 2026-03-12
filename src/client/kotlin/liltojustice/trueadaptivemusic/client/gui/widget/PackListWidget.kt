package liltojustice.trueadaptivemusic.client.gui.widget

import liltojustice.trueadaptivemusic.client.TAMClient
import liltojustice.trueadaptivemusic.client.music.pack.MusicPack
import liltojustice.trueadaptivemusic.client.music.pack.MusicPackValidation
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Tooltip
import net.minecraft.client.gui.components.ObjectSelectionList
import net.minecraft.client.gui.components.Button
import net.minecraft.network.chat.Component
import net.minecraft.util.CommonColors

class PackListWidget(
    private val minecraft: Minecraft,
    width: Int,
    height: Int,
    top: Int,
    itemHeight: Int,
    private val onSelectPack: (selectedPack: MusicPack?) -> Unit = {})
    : ObjectSelectionList<PackListWidget.Entry>(minecraft, width, height, top, itemHeight) {
    init {
        init()
    }

    fun init() {
        clearEntries()
        val vanillaEntry = Entry(this, minecraft, null, onSelectPack)
        addEntry(vanillaEntry)
        setSelected(vanillaEntry)
        MusicPack.loadAllPacks()
            .forEach { musicPack ->
                val newEntry = Entry(this, minecraft, musicPack, onSelectPack)
                addEntry(newEntry)
                if (musicPack.packName == TAMClient.musicPack?.packName) {
                    setSelected(newEntry)
                }
            }
    }

    class Entry(
        private val packListWidget: PackListWidget,
        private val client: Minecraft,
        private val musicPack: MusicPack?,
        private val onSelectPack: (selectedPack: MusicPack?) -> Unit)
        : ObjectSelectionList.Entry<Entry>() {
        private val issuesButton =
            if (musicPack?.validationMessages?.isEmpty() != false)
                null
            else
                Button.Builder(issuesText) {}
                .tooltip(Tooltip.create(getValidationText(musicPack.validationMessages)))
                .width(client.font.width(issuesText) + 5)
                .build()

        override fun render(
            context: GuiGraphics?,
            index: Int,
            y: Int,
            x: Int,
            entryWidth: Int,
            entryHeight: Int,
            mouseX: Int,
            mouseY: Int,
            hovered: Boolean,
            tickDelta: Float
        ) {
            musicPack?.let {
                context?.drawString(
                    client.font, it.packName, x + 3, y + 6, CommonColors.WHITE, false)
                context?.drawString(
                    client.font,
                    it.options.description,
                    x + 3, y + 14 + 3,
                    CommonColors.GRAY,
                    false)

                issuesButton?.let {
                    issuesButton.x = x + entryWidth - issuesButton.width - 5
                    issuesButton.y = y + entryHeight - issuesButton.height - 5
                    issuesButton.render(context, mouseX, mouseY, tickDelta)
                }
            }

            if (musicPack == null) {
                context?.drawString(
                    client.font,
                    Component.translatableWithFallback("trueadaptivemusic.vanilla", "Vanilla"),
                    x + 3,
                    y + 6,
                    CommonColors.WHITE,
                    false)
                context?.drawString(
                    client.font,
                    Component.translatableWithFallback(
                        "trueadaptivemusic.disable_tam", "Disable TrueAdaptiveMusic"),
                    x + 3, y + 14 + 3,
                    CommonColors.GRAY,
                    false)
            }
        }

        override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
            if (packListWidget.getSelected() == this) {
                return true
            }

            if (this.musicPack?.isValid == false)
            {
                return false
            }

            packListWidget.setSelected(this)
            onSelectPack(musicPack)

            return true
        }

        override fun getNarration(): Component {
            return Component.empty()
        }

        companion object {
            private val issuesText = Component.translatableWithFallback(
                "trueadaptivemusic.issues_found", "Issues Found")
            private fun getValidationText(validation: List<MusicPackValidation.ValidationMessage>): Component {
                val warnings = validation.filter { it.type == MusicPackValidation.ValidationMessage.Type.Warning }
                val errors = validation.filter { it.type == MusicPackValidation.ValidationMessage.Type.Error }
                val result = Component.empty()
                if (warnings.isNotEmpty()) {
                    result.append(
                        Component.translatableWithFallback(
                            "trueadaptivemusic.warning_count",
                            "${warnings.size} warning(s)",
                            warnings.size.toString()
                        )
                    )
                }

                if (warnings.isNotEmpty() && errors.isNotEmpty()) {
                    result.append(" ${Component.translatableWithFallback("trueadaptivemusic.and", "and")} ")
                }

                if (errors.isNotEmpty()) {
                    result.append(
                        Component.translatableWithFallback(
                            "trueadaptivemusic.error_count",
                            "${errors.size} error(s)",
                            errors.size.toString()
                        )
                    )
                }

                if (warnings.isNotEmpty() || errors.isNotEmpty()) {
                    result.append("\n\n")
                }

                result.append(validation.joinToString("\n\n") { message -> message.toString() })

                return result
            }
        }
    }
}


