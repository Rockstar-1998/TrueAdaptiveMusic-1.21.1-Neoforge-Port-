package liltojustice.trueadaptivemusic.client.identifier

import net.minecraft.client.Minecraft
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceLocation

class StructureSetIdentifier(id: ResourceLocation): TypedIdentifier(id) {
    override fun toPrefixedTranslationKey(): String {
        return id.toLanguageKey("structure_set")
    }

    companion object: TypedIdentifierCompanion() {
        override fun getRegistryIds(): List<ResourceLocation> {
            val server = Minecraft.getInstance().singleplayerServer ?: return emptyList()
            return server.allLevels
                .flatMap { world ->
                    world.structureManager().registryAccess()
                        .registryOrThrow(Registries.STRUCTURE_SET)
                        .keySet()
                }
                .toSet()
                .toList()
        }
    }
}
