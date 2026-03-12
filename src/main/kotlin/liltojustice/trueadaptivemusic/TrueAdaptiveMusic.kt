package liltojustice.trueadaptivemusic

import net.neoforged.api.distmarker.Dist
import net.neoforged.fml.common.Mod
import net.neoforged.fml.loading.FMLEnvironment
import org.slf4j.LoggerFactory
import java.nio.file.Files
import kotlin.io.path.*

@Mod(TrueAdaptiveMusic.MOD_ID)
class TrueAdaptiveMusic {
    init {
        Files.createDirectories(Constants.MUSIC_PACK_DIR)
        Files.createDirectories(Constants.OPTIONS_DIR)

        if (!Constants.OPTIONS_PATH.exists()) {
            Files.createFile(Constants.OPTIONS_PATH)
        }

        if (FMLEnvironment.dist == Dist.CLIENT) {
            initializeClient()
        }
    }

    companion object {
        const val MOD_ID = "trueadaptivemusic"
        val LOGGER: org.slf4j.Logger = LoggerFactory.getLogger(TrueAdaptiveMusic::class.java)
    }

    private fun initializeClient() {
        try {
            val clazz = Class.forName("liltojustice.trueadaptivemusic.client.TrueAdaptiveMusicClientInitializer")
            val method = clazz.getMethod("init")
            method.invoke(null)
        } catch (e: Exception) {
            LOGGER.error("Failed to initialize client", e)
        }
    }
}
