package liltojustice.trueadaptivemusic.client.javasucks

import liltojustice.trueadaptivemusic.client.TAMClient
import liltojustice.trueadaptivemusic.client.trigger.MusicTrigger
import liltojustice.trueadaptivemusic.client.music.tree.MusicTree
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.network.chat.Component
import net.minecraft.util.CommonColors

object DebugHudMixinHelper {
    private const val INDENT = 10

    @JvmStatic
    fun render(context: GuiGraphics) {
        if (!TAMClient.options.useDebugHud) {
            return
        }

        val musicPack = TAMClient.musicPack ?: return

        val client = Minecraft.getInstance()
        if (client.gui.debugOverlay.showDebugScreen()) {
            return
        }

        val textRenderer = client.font
        val predicateTreeLines = mutableListOf<Line>()
        val rules = musicPack.rules
        val currentNodePath = TAMClient.currentPredicateResult?.path ?: return
        val currentNodePathElements = currentNodePath.split(MusicTree.PATH_SEPARATOR)
        val currentNodeDepth = currentNodePathElements.size

        rules.preorderTraverse { _, path ->
            val text = MusicTrigger.getTruncatedTriggerId(path.last())

            if (path.all { pathElement -> currentNodePathElements.contains(pathElement) }) {
                predicateTreeLines.add(
                    Line(
                        path.size - 1,
                        text,
                        CommonColors.GREEN,
                        currentNodeDepth == path.size
                    )
                )
            }
            else if (path.size <= currentNodeDepth) {
                predicateTreeLines.add(Line(path.size - 1, text))
            }
            else if (path.size - 1 == currentNodeDepth) {
                predicateTreeLines.add(Line(path.size - 1, text))
            }
            else if (path.size - 2 == currentNodeDepth) {
                predicateTreeLines.add(
                    Line(path.size - 1, text.replace(Regex("\\{.*}"), "{...}")))
            }
            else if (path.size - 3 == currentNodeDepth) {
                predicateTreeLines.add(Line(path.size - 1, "..."))
            }
        }

        var rowOffset = 0
        val playingEvent = TAMClient.getPlayingEvent()
        playingEvent?.let {
            context.drawString(
                textRenderer,
                "${Component.translatableWithFallback(
                    "trueadaptivemusic.playing_event", "Playing event").string}: " +
                        playingEvent.getTriggerId(),
                1,
                1,
                CommonColors.WHITE,
                true
            )
            rowOffset += 2
        }

        predicateTreeLines.forEachIndexed { row, line ->
            val fontHeight = textRenderer.lineHeight
            val x: Int = line.indent * INDENT + 1
            val y: Int = (row + rowOffset) * (fontHeight + 2) + 1

            context.drawString(textRenderer, line.text, x, y, line.color, true)

            if (line.selected) {
                context.renderOutline(
                    x - 2,
                    y - 2,
                    textRenderer.width(line.text) + 3,
                    fontHeight + 3, CommonColors.WHITE
                )
            }
        }
    }

    data class Line(val indent: Int, val text: String, val color: Int = CommonColors.WHITE, val selected: Boolean = false)
}



