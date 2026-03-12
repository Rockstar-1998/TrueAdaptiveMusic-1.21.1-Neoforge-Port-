package liltojustice.trueadaptivemusic.client.identifier

import net.minecraft.client.Minecraft
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceLocation

class DimensionIdentifier(id: ResourceLocation): TypedIdentifier(id) {
    override fun toPrefixedTranslationKey(): String {
        return id.toLanguageKey("dimension")
    }

    companion object: TypedIdentifierCompanion() {
        override fun getRegistryIds(): List<ResourceLocation> {
            val level = Minecraft.getInstance().level ?: return listOf()
            return level.registryAccess()
                .registryOrThrow(Registries.DIMENSION_TYPE)
                .keySet()
                .toList()
        }
    }
}
