package liltojustice.trueadaptivemusic.client.sound;

import liltojustice.trueadaptivemusic.client.sound.instance.AudioFileSoundInstance;

/**
 * Helper class to share AudioFileSoundInstance state between Mixins.
 * Mixin classes cannot have public static methods, so we use this helper.
 */
public class AudioStreamHelper {
    private static AudioFileSoundInstance pendingInstance = null;

    public static void setPendingInstance(AudioFileSoundInstance instance) {
        pendingInstance = instance;
    }

    public static AudioFileSoundInstance getPendingInstance() {
        return pendingInstance;
    }

    public static AudioFileSoundInstance consumePendingInstance() {
        AudioFileSoundInstance instance = pendingInstance;
        pendingInstance = null;
        return instance;
    }
}
