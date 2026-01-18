package liltojustice.trueadaptivemusic.client.mixin.event;

import liltojustice.trueadaptivemusic.client.TAMClient;
import liltojustice.trueadaptivemusic.client.trigger.event.MusicEvent;
import liltojustice.trueadaptivemusic.client.trigger.event.types.OnWakeUpEvent;
import net.minecraft.client.gui.screens.InBedChatScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InBedChatScreen.class)
public class OnWakeUpEventMixin {
    /**
     * Inject into removed() which is called when the screen is closed/removed.
     * This triggers when the player wakes up naturally or manually.
     */
    @Inject(at = @At("HEAD"), method = "removed()V")
    public void onRemoved(CallbackInfo ci) {
        MusicEvent.Companion.invokeMusicEvent(TAMClient.INSTANCE.getEventRegistry().get(OnWakeUpEvent.class));
    }
}