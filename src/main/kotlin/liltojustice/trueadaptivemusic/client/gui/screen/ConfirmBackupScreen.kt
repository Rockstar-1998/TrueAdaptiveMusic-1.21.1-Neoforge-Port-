package liltojustice.trueadaptivemusic.client.gui.screen

import liltojustice.trueadaptivemusic.client.music.MusicPack
import net.neoforged.api.distmarker.Dist
import net.neoforged.api.distmarker.OnlyIn
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.gui.components.Button
import net.minecraft.network.chat.Component
import net.minecraft.util.CommonColors
import net.minecraft.resources.ResourceLocation
import java.nio.file.Path
import kotlin.io.path.*

@OnlyIn(Dist.CLIENT)
class ConfirmBackupScreen(
    private val parent: Screen, private val backupPath: Path, private val deleteDestination: Screen)
    : Screen(Component.literal("Backup Exists")) {
    @OptIn(ExperimentalPathApi::class)
    override fun init() {
        val acceptButtonWidget = Button.builder(
            Component.literal("Keep")
        ) {
            minecraft?.setScreen(EditPackScreen(parent, MusicPack.fromFile(backupPath)))
        }.build()
        val deleteButtonWidget = Button.builder(
            Component.literal("Delete")
        ) {
            backupPath.deleteRecursively()
            minecraft?.setScreen(deleteDestination)
        }.build()
        acceptButtonWidget.width = 60
        deleteButtonWidget.width = 60
        acceptButtonWidget.x = width / 2 - 32 - acceptButtonWidget.width / 2
        deleteButtonWidget.x = width / 2 + 32 - deleteButtonWidget.width / 2
        acceptButtonWidget.y = height / 2 + font.lineHeight * 2 + 10
        deleteButtonWidget.y = height / 2 + font.lineHeight * 2 + 10

        addRenderableWidget(acceptButtonWidget)
        addRenderableWidget(deleteButtonWidget)
    }

    override fun onClose() {
        minecraft?.setScreen(parent)
    }

    override fun render(context: GuiGraphics?, mouseX: Int, mouseY: Int, delta: Float) {
        renderBackground(context, mouseX, mouseY, delta)
        super.render(context, mouseX, mouseY, delta)
        context?.drawCenteredString(
            minecraft?.font,
            Component.literal("Unsaved pack edit $backupPath already exists."),
            width / 2,
            height / 2,
            CommonColors.WHITE)
        context?.drawCenteredString(
            minecraft?.font,
            Component.literal("Do you want to keep and continue editing it, or delete it and continue?"),
            width / 2,
            height / 2 + font.lineHeight + 5,
            CommonColors.WHITE)
    }

    companion object {
        private val CHECKMARK: ResourceLocation = ResourceLocation.withDefaultNamespace("icon/checkmark")
    }
}