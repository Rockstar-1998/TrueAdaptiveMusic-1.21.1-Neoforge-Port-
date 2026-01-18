package liltojustice.trueadaptivemusic.client.mixin.event;

import liltojustice.trueadaptivemusic.client.TAMClient;
import liltojustice.trueadaptivemusic.client.trigger.event.MusicEvent;
import liltojustice.trueadaptivemusic.client.trigger.event.types.OnAdvancementGetEvent;
import liltojustice.trueadaptivemusic.client.trigger.event.types.OnRecipeUnlockEvent;
import liltojustice.trueadaptivemusic.client.trigger.event.types.OnTutorialPopupEvent;
import net.minecraft.client.gui.components.toasts.AdvancementToast;
import net.minecraft.client.gui.components.toasts.RecipeToast;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.client.gui.components.toasts.TutorialToast;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ToastComponent.class)
public class OnToastMixin {
    @Inject(at = @At("HEAD"), method = "addToast(Lnet/minecraft/client/gui/components/toasts/Toast;)V")
    public void addToast(Toast toast, CallbackInfo ci) {
        if (toast instanceof AdvancementToast) {
            MusicEvent.Companion
                    .invokeMusicEvent(TAMClient.INSTANCE.getEventRegistry().get(OnAdvancementGetEvent.class));
        } else if (toast instanceof RecipeToast) {
            MusicEvent.Companion.invokeMusicEvent(TAMClient.INSTANCE.getEventRegistry().get(OnRecipeUnlockEvent.class));
        } else if (toast instanceof TutorialToast) {
            MusicEvent.Companion
                    .invokeMusicEvent(TAMClient.INSTANCE.getEventRegistry().get(OnTutorialPopupEvent.class));
        }
    }
}