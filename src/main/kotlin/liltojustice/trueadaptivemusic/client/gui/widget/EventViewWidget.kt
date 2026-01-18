package liltojustice.trueadaptivemusic.client.gui.widget

import liltojustice.trueadaptivemusic.client.TAMClient
import liltojustice.trueadaptivemusic.client.gui.widget.utility.*
import liltojustice.trueadaptivemusic.client.trigger.event.MusicEvent
import liltojustice.trueadaptivemusic.client.music.MusicPack
import liltojustice.trueadaptivemusic.client.trigger.event.ErrorEvent
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.narration.NarrationElementOutput
import net.minecraft.client.gui.components.Tooltip
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.network.chat.Component
import net.minecraft.util.CommonColors
import kotlin.reflect.KParameter
import kotlin.reflect.full.primaryConstructor

class EventViewWidget(
    width: Int,
    height: Int,
    private val musicPack: MusicPack,
    private val onExitView: (newEvent: MusicEvent?) -> Unit,
    x: Int = 0,
    y: Int = 0)
    : ContainerWidget(
    width, height, "Event View", true, false, true, true, x, y) {
    private val eventTypeNameOptions = TAMClient.eventRegistry.getAllNames()
    private var selectedEventTypeName: String = eventTypeNameOptions.firstOrNull() ?: ""
    private var requiredEventArgs = listOf<KParameter>()
    private var eventArgs = mutableListOf<Any?>()
    private val requiredEventParams = MusicEvent.Parameters::class.primaryConstructor?.parameters ?: listOf()
    private var eventParams: MutableList<Any?> = requiredEventParams.map { null }.toMutableList()
    private var selectedEvent: MusicEvent? = null
    private var selectedMusicPaths = mutableListOf<String>()
    private var assets = musicPack.getEditPackAssets()

    init {
        addBackButton { onExitView(selectedEvent) }
    }

    fun setEvent(event: MusicEvent?) {
        selectedEvent = event
        eventParams = selectedEvent?.parameters?.constructorParams()?.toMutableList()
            ?: requiredEventParams.map { null }.toMutableList()
        if (event != null) {
            setSelectedEventTypeName(event.getTypeName())
            eventArgs = (event.getTriggerArgs().map { param -> param.value }).toMutableList()
            selectedMusicPaths = event.playableSounds.map { sound -> sound.getSoundName() }.toMutableList()
        }
        else {
            setSelectedEventTypeName(eventTypeNameOptions.firstOrNull() ?: "")
            selectedMusicPaths = mutableListOf()
        }
    }

    override fun updateWidgetNarration(narrationOutput: NarrationElementOutput) {
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (isMouseOver(mouseX, mouseY)) {
            screen?.focused = null
        }

        return super.mouseClicked(mouseX, mouseY, button)
    }

    override fun renderWidget(context: GuiGraphics?, mouseX: Int, mouseY: Int, delta: Float) {
        super.renderWidget(context, mouseX, mouseY, delta)
        if (selectedEvent is ErrorEvent) {
            addWidgetFromRender(
                {
                    ClickableTextWidget(
                        Component.literal("Delete").string,
                        onClick = {
                            exit(null)
                        }
                    )
                },
                "Delete"
            )

            return
        }

        if (!visible) {
            return
        }

        addWidgetFromRender(
            {
                DropdownWidget(
                    eventTypeNameOptions,
                    { typeName ->  setSelectedEventTypeName(typeName) },
                    width / 2,
                    Component.literal("Type").string,
                    startingOption = selectedEventTypeName)
            },
            "eventTypeChoice",
            row = 1)

        val musicDropdownWidget = addWidgetFromRender(
            {
                MultiSelectDropdownWidget(
                    listOf(),
                    width,
                    { selected -> selectedMusicPaths = selected.toMutableList() },
                    Component.literal("Music Choice").string,
                    {
                        musicPack.getEditPackAssets().map { (assetName, _) -> assetName }.toMutableSet()
                            .union(
                                BuiltInRegistries.SOUND_EVENT.keySet()
                                    .map { id -> id.toString() }
                                    .filter { path -> path.contains("music.") }).toList()
                    },
                    Component.literal("Select a track").string,
                    selectedMusicPaths,
                    onHoverOption = { option ->
                        TAMClient.playSoundNow(option.let { MusicPack.toPlayableSound(assets, it) }) })
            },
            "musicChoice"
        )

        if (isMouseOver(mouseX.toDouble(), mouseY.toDouble())
            && !musicDropdownWidget.isMouseOver(mouseX.toDouble(), mouseY.toDouble())) {
            TAMClient.playSoundNow(null)
        }

        requiredEventArgs.forEach { arg ->
            addWidgetFromRender(
                { TAMClient.makeInputWidget(screen!!, eventArgs, arg) },
                "eventArg: ${arg.name ?: arg.index}"
            )
        }

        requiredEventParams.forEach { param ->
            addWidgetFromRender(
                { TAMClient.makeInputWidget(screen!!, eventParams, param) },
                "eventParam: ${param.name ?: param.index}"
            )
        }

        val saveWidget = addWidgetFromRender(
            {
                ClickableTextWidget(
                    "Save",
                    onClick = {
                        assets = musicPack.getEditPackAssets()
                        val newEvent = TAMClient.eventFactory
                            .fromArgs(
                                selectedEventTypeName,
                                selectedMusicPaths
                                    .mapNotNull { path -> MusicPack.toPlayableSound(assets, path) },
                                eventParams.filterNotNull(),
                                eventArgs.filterNotNull())

                        exit(newEvent)
                    })
            },
            "Save"
        ) as ClickableTextWidget

        if (selectedEvent != null) {
            addWidgetFromRender(
                {
                    ClickableTextWidget(
                        Component.literal("Delete").string,
                        onClick = {
                            exit(null)
                        }
                    )
                },
                "Delete"
            )
        }

        saveWidget.active = eventArgs.filterNotNull().size == requiredEventArgs.size
        saveWidget.color = if (saveWidget.active) CommonColors.WHITE else CommonColors.RED
        saveWidget.tooltip =
            if (saveWidget.active)
                null
            else
                MISSING_ARGS_TOOLTIP
    }

    private fun setSelectedEventTypeName(typeName: String) {
        if (selectedEventTypeName == typeName) {
            return
        }

        selectedEventTypeName = typeName
        requiredEventArgs = TAMClient.eventFactory.getRequiredArgs(typeName)
        eventArgs = requiredEventArgs.map { null }.toMutableList()
        clearWidgetsFromRender()
    }

    private fun exit(event: MusicEvent?) {
        selectedEvent = null
        selectedMusicPaths = mutableListOf()
        selectedEventTypeName = eventTypeNameOptions.firstOrNull() ?: ""
        requiredEventArgs = emptyList()
        eventArgs = mutableListOf()
        clearWidgetsFromRender()
        onExitView(event)
    }

    companion object {
        private val MISSING_ARGS_TOOLTIP =
            Tooltip.create(
                Component.literal("At least one required parameter for this type is missing."))
    }
}