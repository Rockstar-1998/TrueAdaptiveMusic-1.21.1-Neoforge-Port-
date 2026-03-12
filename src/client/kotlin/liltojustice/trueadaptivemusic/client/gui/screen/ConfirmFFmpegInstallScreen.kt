package liltojustice.trueadaptivemusic.client.gui.screen

import liltojustice.trueadaptivemusic.Constants
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.components.SpriteIconButton
import net.minecraft.network.chat.CommonComponents
import net.minecraft.network.chat.Component
import net.minecraft.util.CommonColors
import net.minecraft.resources.ResourceLocation
import net.minecraft.Util
import kotlin.io.path.*

class ConfirmFFmpegInstallScreen(private val parent: Screen)
    : Screen(
    Component.translatableWithFallback(
        "trueadaptivemusic.ffmpeg_install", "Install FFmpeg").append("?")
    ) {
    @OptIn(ExperimentalPathApi::class)
    override fun init() {
        val acceptButtonWidget = SpriteIconButton.builder(
            CommonComponents.GUI_PROCEED,
            {
                Util.getPlatform().openUri(Constants.FFMPEG_DOWNLOAD_LINK)
                onClose()
            },
            false)
            .sprite(CHECKMARK, 9, 8)
            .build()
        val backButtonWidget = Button.Builder(CommonComponents.GUI_BACK) { onClose() }.build()
        acceptButtonWidget.width = font.width(acceptButtonWidget.message) + 20
        backButtonWidget.width = font.width(backButtonWidget.message) + 10
        acceptButtonWidget.x = width / 2 - 32 - acceptButtonWidget.width / 2
        acceptButtonWidget.y = height / 2 + font.lineHeight * 2 + 10
        backButtonWidget.x = width / 2 + 32 - backButtonWidget.width / 2
        backButtonWidget.y = height / 2 + font.lineHeight * 2 + 10

        addRenderableWidget(acceptButtonWidget)
        addRenderableWidget(backButtonWidget)
    }

    override fun onClose() {
        minecraft?.setScreen(parent)
    }

    override fun render(context: GuiGraphics?, mouseX: Int, mouseY: Int, delta: Float) {
        super.render(context, mouseX, mouseY, delta)
        context?.drawCenteredString(
            font,
            Component.translatableWithFallback(
                "trueadaptivemusic.ffmpeg_instruction",
                "ffmpeg.exe and ffprobe.exe should be placed in").string
                    + ' ' + Constants.OPTIONS_DIR.pathString,
            width / 2,
            height / 2,
            CommonColors.WHITE)
        context?.drawCenteredString(
            font,
            Component.translatableWithFallback(
                "trueadaptivemusic.ffmpeg_agree", "Would you like to open the link to download FFmpeg?"),
            width / 2,
            height / 2 + font.lineHeight + 5,
            CommonColors.WHITE)
    }

    companion object {
        private val CHECKMARK: ResourceLocation = ResourceLocation.withDefaultNamespace("icon/checkmark")
    }
}




