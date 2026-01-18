package liltojustice.trueadaptivemusic.client.javasucks

import liltojustice.trueadaptivemusic.Constants
import liltojustice.trueadaptivemusic.client.TAMClient
import liltojustice.trueadaptivemusic.client.trigger.MusicTrigger
import liltojustice.trueadaptivemusic.client.trigger.predicate.MusicPredicateTree
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

        val minecraft = Minecraft.getInstance()
        if (minecraft.gui.debugOverlay.showDebugScreen()) {
            return
        }

        val font = minecraft.font
        val predicateTreeLines = mutableListOf<Line>()
        val rules = musicPack.rules
        val currentNodePath = rules.getMusicToPlay(minecraft).path
        val currentNodeDepth = currentNodePath.split(MusicPredicateTree.PATH_SEPARATOR).size

        rules.preorderTraverse { _, path ->
            val pathString = path.joinToString(MusicPredicateTree.PATH_SEPARATOR)
            val text = MusicTrigger.getTruncatedTriggerId(path.last())

            if (currentNodePath.contains(pathString)) {
                predicateTreeLines.add(Line(path.size - 1, pathString, text, Constants.CommonColors.GREEN))
            }
            else if (path.size <= currentNodeDepth) {
                predicateTreeLines.add(Line(path.size - 1, pathString, text))
            }
            else if (path.size - 1 == currentNodeDepth) {
                predicateTreeLines.add(Line(path.size - 1, pathString, text))
            }
            else if (path.size - 2 == currentNodeDepth) {
                predicateTreeLines.add(
                    Line(path.size - 1, pathString, text.replace(Regex("\\{.*}"), "{...}")))
            }
            else if (path.size - 3 == currentNodeDepth) {
                predicateTreeLines.add(Line(path.size - 1, pathString, "..."))
            }
        }

        var rowOffset = 0
        val playingEvent = TAMClient.getPlayingEvent()
        playingEvent?.let {
            context.drawString(
                font,
                "${Component.literal("Playing event").string}: " +
                        playingEvent.getTriggerId(),
                1,
                1,
                CommonColors.WHITE,
                true)
            rowOffset += 2
        }

        predicateTreeLines.forEachIndexed { row, line ->
            val fontHeight = font.lineHeight
            val x: Int = line.indent * INDENT + 1
            val y: Int = (row + rowOffset) * (fontHeight + 2) + 1

            context.drawString(font, line.text, x, y, line.color, true)

            if (line.path == currentNodePath) {
                context.renderOutline(
                    x - 2,
                    y - 2,
                    font.width(line.text) + 3,
                    fontHeight + 3,
                    CommonColors.WHITE)
            }
        }
    }

    data class Line(val indent: Int, val path: String, val text: String, val color: Int = CommonColors.WHITE)
}
