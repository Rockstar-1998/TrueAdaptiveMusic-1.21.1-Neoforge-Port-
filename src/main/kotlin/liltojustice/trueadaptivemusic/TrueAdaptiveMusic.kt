package liltojustice.trueadaptivemusic

import net.neoforged.bus.api.IEventBus
import net.neoforged.fml.common.Mod
import org.slf4j.LoggerFactory
import java.nio.file.Files
import kotlin.io.path.*

@Mod(TrueAdaptiveMusic.MOD_ID)
class TrueAdaptiveMusic(modEventBus: IEventBus) {
    
    init {
        // Initialize directories and options file on mod construction
        onInitialize()
    }
    
    @OptIn(ExperimentalPathApi::class)
    private fun onInitialize() {
        Files.createDirectories(Path(Constants.MUSIC_PACK_DIR))

        val optionsFilePath = Path(Constants.OPTIONS_FILENAME)

        if (!optionsFilePath.exists()) {
            Files.createFile(optionsFilePath)
        }
    }

    companion object {
        const val MOD_ID = "trueadaptivemusic"
        val LOGGER: org.slf4j.Logger = LoggerFactory.getLogger(TrueAdaptiveMusic::class.java)
    }
}
