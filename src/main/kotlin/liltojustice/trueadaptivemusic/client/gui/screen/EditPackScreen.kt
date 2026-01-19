package liltojustice.trueadaptivemusic.client.gui.screen

import liltojustice.trueadaptivemusic.client.TAMClient
import liltojustice.trueadaptivemusic.client.trigger.event.MusicEvent
import liltojustice.trueadaptivemusic.client.gui.widget.EventViewWidget
import liltojustice.trueadaptivemusic.client.gui.widget.PackStructureWidget
import liltojustice.trueadaptivemusic.client.gui.widget.PredicateViewWidget
import liltojustice.trueadaptivemusic.client.music.MusicPack
import net.neoforged.api.distmarker.Dist
import net.neoforged.api.distmarker.OnlyIn
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.layouts.FrameLayout
import net.minecraft.client.gui.layouts.GridLayout
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.gui.components.Tooltip
import net.minecraft.client.gui.components.*
import net.minecraft.network.chat.Component
import net.minecraft.util.CommonColors
import net.minecraft.resources.ResourceLocation
import net.minecraft.Util

@OnlyIn(Dist.CLIENT)
class EditPackScreen(private val parent: Screen, private val musicPack: MusicPack)
    : Screen(
    Component.literal("Create/Edit a music pack")) {
    private lateinit var predicateViewWidget: PredicateViewWidget
    private lateinit var packStructureWidget: PackStructureWidget
    private lateinit var eventViewWidget: EventViewWidget
    private lateinit var saveButtonWidget: Button
    private lateinit var closeButtonWidget: Button
    private lateinit var openAssetsFolderButtonWidget: Button
    private var selectedEvent: MusicEvent? = null

    private val eventView: Boolean
        get() = eventViewWidget.visible

    private fun initPack() {
        TAMClient.playSoundNow(null)
        val newPath = musicPack.initEdit(musicPack)
        TAMClient.musicPack = MusicPack.fromFile(newPath)
    }

    override fun init() {
        initPack()

        saveButtonWidget = Button.builder(SAVE_BUTTON_TEXT) {
            TAMClient.musicPack = null
            val path = musicPack.save()
            TAMClient.musicPack = MusicPack.fromFile(path)
            this.onClose()
        }.build()

        closeButtonWidget = Button.builder(CLOSE_BUTTON_TEXT) {
            onClose()
        }
            .build()

        openAssetsFolderButtonWidget = Button.builder(OPEN_ASSETS_TEXT) {
            Util.getPlatform().openUri(musicPack.getEditPackAssetsPath().toUri())
        }
            .build()

        predicateViewWidget = PredicateViewWidget(
            getContainerWidth(),
            getContainerHeight(),
            musicPack,
            {
                initPack()
                packStructureWidget.initPredicateWidgets()
            },
            { event -> switchToEventView(event) },
            { eventView })
        packStructureWidget = PackStructureWidget(
            getContainerWidth(),
            getContainerHeight(),
            musicPack,
            {
                initPack()
                packStructureWidget.initPredicateWidgets()
            },
            { node ->
                predicateViewWidget.setEditExistingNode(node)
                switchToPredicateView()
            },
            { parent ->
                predicateViewWidget.setCreateNewNode(parent)
                switchToPredicateView()
            })
        eventViewWidget = EventViewWidget(
            getContainerWidth(),
            getContainerHeight(),
            musicPack,
            { newEvent ->
                predicateViewWidget.onEventModeExit(newEvent)
                switchToPredicateView() }
        )

        addRenderableWidget(saveButtonWidget)
        addRenderableWidget(closeButtonWidget)
        addRenderableWidget(openAssetsFolderButtonWidget)
        addRenderableWidget(predicateViewWidget)
        addRenderableWidget(packStructureWidget)
        addRenderableWidget(eventViewWidget)

        saveButtonWidget.width = 90
        closeButtonWidget.x = saveButtonWidget.x + saveButtonWidget.width + 5
        closeButtonWidget.width = font.width(CLOSE_BUTTON_TEXT) + 10
        closeButtonWidget.tooltip = Tooltip.create(
            Component.literal("Changes will be saved"))
        openAssetsFolderButtonWidget.width = font.width(OPEN_ASSETS_TEXT) + 10
        openAssetsFolderButtonWidget.x = width - openAssetsFolderButtonWidget.width

        val containerWidth = getContainerWidth()
        val containerHeight = getContainerHeight()
        packStructureWidget.width = containerWidth
        packStructureWidget.height = containerHeight
        predicateViewWidget.width = containerWidth
        predicateViewWidget.height = containerHeight
        eventViewWidget.width = containerWidth
        eventViewWidget.height = containerHeight

        if (selectedEvent == null) {
            switchToPredicateView()
        }
        else {
            switchToEventView(selectedEvent)
        }
    }

    override fun onClose() {
        if (parent is MainScreen) {
            parent.reload()
        }

        TAMClient.refreshCurrentMusicPack()
        minecraft?.setScreen(parent)
    }

    override fun render(context: GuiGraphics?, mouseX: Int, mouseY: Int, delta: Float) {
        renderBackground(context, mouseX, mouseY, delta)
        super.render(context, mouseX, mouseY, delta)
        context?.drawCenteredString(
            this.font, this.title, this.width / 2, 22, CommonColors.WHITE)
    }

    private fun positionContainers() {
        val spacing = LEFT_MARGIN / 2
        val containerWidth = getContainerWidth()
        val containerY = TOP_MARGIN
        
        if (eventView) {
            predicateViewWidget.x = LEFT_MARGIN
            predicateViewWidget.y = containerY
            predicateViewWidget.width = containerWidth
            
            eventViewWidget.x = LEFT_MARGIN + containerWidth + spacing
            eventViewWidget.y = containerY
            eventViewWidget.width = containerWidth
        }
        else {
            packStructureWidget.x = LEFT_MARGIN
            packStructureWidget.y = containerY
            packStructureWidget.width = containerWidth
            
            predicateViewWidget.x = LEFT_MARGIN + containerWidth + spacing
            predicateViewWidget.y = containerY
            predicateViewWidget.width = containerWidth
        }
    }

    private fun switchToEventView(event: MusicEvent?) {
        eventViewWidget.visible = true
        packStructureWidget.visible = false
        eventViewWidget.setEvent(event)
        positionContainers()
    }

    private fun switchToPredicateView() {
        packStructureWidget.visible = true
        eventViewWidget.visible = false
        positionContainers()
    }

    private fun getContainerWidth(): Int {
        return (width * 0.5 - LEFT_MARGIN - RIGHT_MARGIN).toInt()
    }

    private fun getContainerHeight(): Int {
        return (height - TOP_MARGIN - BOTTOM_MARGIN)
    }

    companion object {
        private val CHECKMARK: ResourceLocation = ResourceLocation.withDefaultNamespace("icon/checkmark")
        private const val TOP_MARGIN = 32
        private const val BOTTOM_MARGIN = TOP_MARGIN / 4
        private const val LEFT_MARGIN = TOP_MARGIN / 4
        private const val RIGHT_MARGIN = LEFT_MARGIN
        private val OPEN_ASSETS_TEXT = Component.literal("Show Assets")
        private val SAVE_BUTTON_TEXT = Component.literal("Save and Zip")
        private val CLOSE_BUTTON_TEXT = Component.literal("Close")
    }
}