package liltojustice.trueadaptivemusic.client.music.pack.meta

import com.google.gson.GsonBuilder
import liltojustice.trueadaptivemusic.client.music.tree.MusicTree
import net.neoforged.fml.ModList

data class MusicPackMeta(val requiredBridgeMods: List<ModDependency> = emptyList()) {
    fun jsonEncode(): String {
        return json.toJson(this)
    }

    companion object {
        private val json = GsonBuilder()
            .setPrettyPrinting()
            .create()

        fun jsonDecode(string: String): MusicPackMeta {
            return json.fromJson(string, MusicPackMeta::class.java)
        }

        fun init(rules: MusicTree): MusicPackMeta {
            return MusicPackMeta(getRequiredBridgeMods(rules))
        }

        private fun getRequiredBridgeMods(rules: MusicTree): List<ModDependency> {
            val requiredBridgeMods = mutableListOf<ModDependency>()
            val packageNames = mutableSetOf<String>()
            rules.traverse { node, _ ->
                node.predicates.forEach { predicate ->
                    packageNames.add(predicate::class.java.packageName)
                }
            }

            ModList.get().mods.forEach { modInfo ->
                if (packageNames.any { packageName -> packageName.contains(modInfo.modId) }) {
                    requiredBridgeMods.add(ModDependency(modInfo.modId, modInfo.displayName))
                }
            }

            return requiredBridgeMods.toList()
        }
    }
}
