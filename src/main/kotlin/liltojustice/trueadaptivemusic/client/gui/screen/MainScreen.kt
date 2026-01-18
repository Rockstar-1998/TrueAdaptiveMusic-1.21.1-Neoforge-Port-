package liltojustice.trueadaptivemusic.client.gui.screen

import liltojustice.trueadaptivemusic.Constants
import liltojustice.trueadaptivemusic.client.TAMClient
import liltojustice.trueadaptivemusic.client.gui.widget.PackListWidget
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.gui.components.Button
import net.minecraft.network.chat.CommonComponents
import net.minecraft.network.chat.Component
import net.minecraft.util.CommonColors
import net.minecraft.Util
import java.nio.file.Path
import kotlin.io.path.*

@Environment(EnvType.CLIENT)
class MainScreen(private val parent: Screen): Screen(
    Component.literal("Music Packs")) {
    private lateinit var createNewPackButton: Button
    private lateinit var packListWidget: PackListWidget
    private lateinit var openMusicPacksButton: Button
    private lateinit var doneButton: Button
    private lateinit var editButton: Button
    private lateinit var refreshButton: Button
    private lateinit var wikiButton: Button
    private lateinit var optionsButton: Button

    override fun init() {
        TAMClient.playSoundNow(null)
        createNewPackButton = Button.builder(CREATE_PACK_TEXT)
        {
            minecraft?.setScreen(PackNameScreen(this))
        }.build()
        createNewPackButton.width = font.width(CREATE_PACK_TEXT) + 10

        openMusicPacksButton = Button.builder(OPEN_MUSIC_PACKS_TEXT) {
            Util.getPlatform().openUri(Path(Constants.MUSIC_PACK_DIR).toUri())
        }.build()
        openMusicPacksButton.width = font.width(OPEN_MUSIC_PACKS_TEXT) + 10
        openMusicPacksButton.x = width - openMusicPacksButton.width

        packListWidget = PackListWidget(minecraft!!, this.width, this.height - 96, 48, 36)
        { musicPack ->
            TAMClient.musicPack = musicPack
            editButton.visible = musicPack != null
        }

        doneButton = Button.builder(CommonComponents.GUI_DONE) { _: Button? -> minecraft?.setScreen(parent) }.build()
        doneButton.width = font.width(CommonComponents.GUI_DONE) + 10
        doneButton.x = width - doneButton.width
        doneButton.y = height - doneButton.height

        editButton = Button.builder(EDIT_TEXT)
        {
            val currentPack = TAMClient.musicPack!!
            val ongoingEdit = getOngoingEdit(Path(currentPack.packName))
            val editScreen = EditPackScreen(this, currentPack)
            if (ongoingEdit != null && ongoingEdit.name != currentPack.packName) {
                minecraft?.setScreen(ConfirmBackupScreen(this, ongoingEdit, editScreen))
                return@builder
            }

            minecraft?.setScreen(editScreen)
        }.build()
        editButton.width = font.width(EDIT_TEXT) + 10
        editButton.y = height - editButton.height
        editButton.visible = TAMClient.musicPack != null

        refreshButton = Button.builder(REFRESH_TEXT) { _: Button? -> reload() }.build()
        refreshButton.y = createNewPackButton.y + createNewPackButton.height + 5
        refreshButton.width = font.width(REFRESH_TEXT) + 10

        wikiButton = Button.builder(WIKI_TEXT)
        { _: Button? -> Util.getPlatform().openUri(Constants.WIKI_LINK) }.build()
        wikiButton.y = openMusicPacksButton.y + openMusicPacksButton.height + 5
        wikiButton.width = font.width(WIKI_TEXT) + 10
        wikiButton.x = width - wikiButton.width

        optionsButton = Button.builder(OPTIONS_TEXT)
        { _: Button? -> minecraft?.setScreen(OptionsScreen(this)) }.build()
        optionsButton.y = doneButton.y - doneButton.height - 5
        optionsButton.width = font.width(OPTIONS_TEXT) + 10
        optionsButton.x = width - optionsButton.width

        addWidget(packListWidget)
        addRenderableWidget(createNewPackButton)
        addRenderableWidget(openMusicPacksButton)
        addRenderableWidget(doneButton)
        addRenderableWidget(editButton)
        addRenderableWidget(refreshButton)
        addRenderableWidget(wikiButton)
        addRenderableWidget(optionsButton)
    }

    override fun onClose() {
        minecraft?.setScreen(parent)
    }

    override fun render(context: GuiGraphics?, mouseX: Int, mouseY: Int, delta: Float) {
        super.render(context, mouseX, mouseY, delta)
        this.packListWidget.render(context, mouseX, mouseY, delta)
        context?.drawCenteredString(
            this.font, this.title, this.width / 2, 28, CommonColors.WHITE)
    }

    fun reload() {
        packListWidget.init()
    }

    companion object {
        fun getOngoingEdit(packName: Path): Path? {
            return Path(Constants.MUSIC_PACK_DIR)
                .listDirectoryEntries()
                .firstOrNull() { file ->
                    packName.nameWithoutExtension == file.nameWithoutExtension && file.extension == "new" }
        }

        private val OPEN_MUSIC_PACKS_TEXT = Component.literal("Open Pack Folder")
        private val CREATE_PACK_TEXT = Component.literal("Create a new music pack")
        private val REFRESH_TEXT = Component.literal("Refresh")
        private val EDIT_TEXT = Component.literal("Edit Pack")
        private val WIKI_TEXT = Component.literal("Open Wiki")
        private val OPTIONS_TEXT = Component.literal("Options")
    }
}