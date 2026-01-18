package liltojustice.trueadaptivemusic.client;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.world.InteractionResult;
import org.jetbrains.annotations.Nullable;

public interface InvokeMusicEventCallback {
    Event<InvokeMusicEventCallback> EVENT = EventFactory.createArrayBacked(InvokeMusicEventCallback.class,
            (listeners) -> (eventType, args) -> {
                for (InvokeMusicEventCallback listener : listeners) {
                    InteractionResult result = listener.invokeMusicEvent(eventType, args);

                    if (result != InteractionResult.PASS) {
                        return result;
                    }
                }

                return InteractionResult.PASS;
            });

    InteractionResult invokeMusicEvent(String eventType, @Nullable Object... eventArgs);
}