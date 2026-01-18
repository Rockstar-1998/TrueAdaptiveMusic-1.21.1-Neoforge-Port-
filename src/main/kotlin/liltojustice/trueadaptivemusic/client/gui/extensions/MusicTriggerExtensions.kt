package liltojustice.trueadaptivemusic.client.gui.extensions

import liltojustice.trueadaptivemusic.client.trigger.MusicTrigger
import liltojustice.trueadaptivemusic.client.trigger.event.ErrorEvent
import liltojustice.trueadaptivemusic.client.trigger.predicate.ErrorPredicate
import net.minecraft.network.chat.Component

fun MusicTrigger<*>.getTriggerTooltipString(): String {
    return getTriggerTooltipText().string
}

fun MusicTrigger<*>.getTriggerTooltipText(): Component {
    if (this is ErrorPredicate) {
        return Component.literal(Component.literal("Failed to load this predicate, so it will always be false.").string +
                    "\n\n${Component.literal("Reason").string}" +
                ": $reason\n\nJson: $shortenedJson")
    }

    if (this is ErrorEvent) {
        return Component.literal(Component.literal("Failed to load this event, so it will never trigger.").string + "\n\n${Component.literal("Reason")}: $reason\n\nJson: $shortenedJson")
    }

    val result = StringBuilder()
    val args = getTriggerArgs()
    args.forEach { param -> result.appendLine(param.toString()) }

    if (args.isEmpty()) {
        result.append(
            Component.literal("No Arguments").string)
    }

    return Component.literal(result.trim().toString())
}
