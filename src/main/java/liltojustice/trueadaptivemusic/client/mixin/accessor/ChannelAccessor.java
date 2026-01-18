package liltojustice.trueadaptivemusic.client.mixin.accessor;

import com.mojang.blaze3d.audio.Channel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Accessor mixin for Channel to access the OpenAL source ID.
 * This allows us to directly control the volume via OpenAL API.
 */
@Mixin(Channel.class)
public interface ChannelAccessor {
    @Accessor("source")
    int getSource();
}
