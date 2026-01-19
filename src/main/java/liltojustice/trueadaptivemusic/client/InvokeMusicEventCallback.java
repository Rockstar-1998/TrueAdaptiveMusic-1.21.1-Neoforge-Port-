package liltojustice.trueadaptivemusic.client;

import net.minecraft.world.InteractionResult;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Custom event callback system for invoking music events.
 * Replaces Fabric's EventFactory with a simple thread-safe callback list.
 */
public interface InvokeMusicEventCallback {
    /**
     * Thread-safe list of registered listeners.
     */
    List<InvokeMusicEventCallback> LISTENERS = new CopyOnWriteArrayList<>();

    /**
     * Invoke all registered listeners with the given event.
     *
     * @param eventType The type of music event
     * @param eventArgs Optional arguments for the event
     * @return The result of the event invocation
     */
    static InteractionResult invoke(String eventType, @Nullable Object... eventArgs) {
        for (InvokeMusicEventCallback listener : LISTENERS) {
            InteractionResult result = listener.invokeMusicEvent(eventType, eventArgs);
            if (result != InteractionResult.PASS) {
                return result;
            }
        }
        return InteractionResult.PASS;
    }

    /**
     * Register a new listener.
     *
     * @param listener The listener to register
     */
    static void register(InvokeMusicEventCallback listener) {
        LISTENERS.add(listener);
    }

    /**
     * Handle a music event invocation.
     *
     * @param eventType The type of music event
     * @param eventArgs Optional arguments for the event
     * @return The result of handling the event
     */
    InteractionResult invokeMusicEvent(String eventType, @Nullable Object... eventArgs);
}