package liltojustice.trueadaptivemusic.client.mixin.event;

import liltojustice.trueadaptivemusic.client.TAMClient;
import liltojustice.trueadaptivemusic.client.trigger.event.MusicEvent;
import liltojustice.trueadaptivemusic.client.trigger.event.types.OnJoinWorldEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class OnJoinWorldEventMixin {
    /**
     * Inject into updateLevelInEngines which is called when the level changes.
     * In NeoForge 1.21.1, this replaces the old setLevel method.
     */
    @Inject(at = @At("HEAD"), method = "updateLevelInEngines")
    public void onUpdateLevelInEngines(ClientLevel level, CallbackInfo ci) {
        if (level != null) {
            MusicEvent.Companion.invokeMusicEvent(TAMClient.INSTANCE.getEventRegistry().get(OnJoinWorldEvent.class));
        }
    }
}