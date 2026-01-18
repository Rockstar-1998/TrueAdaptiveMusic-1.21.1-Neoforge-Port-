package liltojustice.trueadaptivemusic.client.javasucks;

import liltojustice.trueadaptivemusic.client.mixin.accessor.BossHealthOverlayAccessor;
import net.minecraft.client.gui.components.BossHealthOverlay;
import net.minecraft.client.gui.components.LerpingBossEvent;

import java.util.Map;
import java.util.UUID;

/**
 * Helper class to access package-private fields in BossHealthOverlay
 * using Mixin accessor interface.
 */
public class BossEventHelper {
    /**
     * Gets the events map from BossHealthOverlay.
     * Uses Mixin accessor to access the private field.
     * 
     * @param overlay The BossHealthOverlay instance
     * @return The map of boss events
     */
    public static Map<UUID, LerpingBossEvent> getEvents(BossHealthOverlay overlay) {
        return ((BossHealthOverlayAccessor) overlay).getEvents();
    }
}
