package liltojustice.trueadaptivemusic.client.gui.screen

import liltojustice.trueadaptivemusic.Constants
import liltojustice.trueadaptivemusic.client.music.MusicPack
import net.neoforged.api.distmarker.Dist
import net.neoforged.api.distmarker.OnlyIn
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.gui.components.EditBox
import net.minecraft.client.gui.components.Button
import net.minecraft.network.chat.Component
import net.minecraft.util.CommonColors
import net.minecraft.resources.ResourceLocation
import kotlin.io.path.Path
import kotlin.io.path.exists

@OnlyIn(Dist.CLIENT)
class PackNameScreen(private val parent: Screen): Screen(
    Component.literal("Name Your New Pack")) {
    private var packName = ""
    private var errorText = ""
    private lateinit var packNameWidget: EditBox
    private lateinit var acceptButtonWidget: Button

    override fun init() {
        packNameWidget = EditBox(
            minecraft!!.font,
            width / 2 - width / 6,
            height / 2,
            width / 3,
            (minecraft?.font?.lineHeight ?: 0) + 5,
            Component.literal("Pack Name")
        )
        packNameWidget.setResponder { name ->
            errorText = ""
            this.packName = name
            if (Path(Constants.MUSIC_PACK_DIR, "$name.zip").exists()) {
                errorText = "$name.zip already exists"
            }
        }
        acceptButtonWidget = Button.builder(
            Component.literal("Accept")
        ) {
            if (!validPackName(packName) || errorText.isNotEmpty()) {
                return@builder
            }
            minecraft?.setScreen(EditPackScreen(parent, MusicPack.makeEmpty(packName)))
        }.build()
        acceptButtonWidget.width = 60
        acceptButtonWidget.x = width / 2 - width / 6
        acceptButtonWidget.y = height / 2 + (minecraft?.font?.lineHeight ?: 0) + 10

        addRenderableWidget(packNameWidget)
        addRenderableWidget(acceptButtonWidget)
    }

    override fun onClose() {
        minecraft?.setScreen(parent)
    }

    override fun render(context: GuiGraphics?, mouseX: Int, mouseY: Int, delta: Float) {
        renderBackground(context, mouseX, mouseY, delta)
        context?.drawString(
            minecraft?.font,
            errorText,
            width / 2 - width / 6,
            height / 2 + (minecraft?.font?.lineHeight ?: 0) + 35,
            CommonColors.RED,
            false)
        context?.drawCenteredString(
            minecraft?.font,
            Component.literal("Name Your New Pack"),
            width / 2,
            10,
            CommonColors.WHITE)
        acceptButtonWidget.active = errorText.isEmpty() && validPackName(packName)
        super.render(context, mouseX, mouseY, delta)
    }

    companion object {
        private val CHECKMARK: ResourceLocation = ResourceLocation.withDefaultNamespace("icon/checkmark")

        fun validPackName(packName: String): Boolean {
            if (packName.isEmpty()) {
                return false
            }

            try {
                Path(Constants.MUSIC_PACK_DIR, "$packName.zip")
            } catch (_: Exception) {
                return false
            }

            return true
        }
    }
}