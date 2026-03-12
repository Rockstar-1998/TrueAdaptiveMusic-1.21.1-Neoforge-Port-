package liltojustice.trueadaptivemusic.client;

import com.mojang.serialization.Codec;
import liltojustice.trueadaptivemusic.client.gui.screen.MainScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import fgo.fgo;
import fim.fim;
import fik.fik;
import fgs.fgs;
import fgr.fgr;
import wz.wz;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

@Environment(EnvType.CLIENT)
public record TrueAdaptiveMusicOptionCallback<T>(fgo client)
        implements fgr.n<T> {
    @Override
    public Function<fgr<T>, fik> getWidgetCreator(
            fgr.l<T> tooltipFactory,
            fgs gameOptions,
            int x,
            int y,
            int width,
            Consumer<T> changeCallback) {
        return option -> {
            assert client.currentScreen != null;
            return new fim.Builder(
                    wz.translatableWithFallback(
                            "trueadaptivemusic.trueadaptivemusic",
                            "True Adaptive Music"
                    ),
                    widget -> client.setScreen(new MainScreen(client.currentScreen))
            ).dimensions(x, y, width, 20).build();
        };
    }

    @Override
    public Optional<T> validate(T value) {
        return Optional.of(value);
    }

    @Override
    public Codec<T> codec() {
        return null;
    }
}
