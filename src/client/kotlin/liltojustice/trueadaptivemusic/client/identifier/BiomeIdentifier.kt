package liltojustice.trueadaptivemusic.client.identifier

import net.minecraft.client.Minecraft
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceLocation

class BiomeIdentifier(id: ResourceLocation): TypedIdentifier(id) {
    override fun toPrefixedTranslationKey(): String {
        return id.toLanguageKey("biome")
    }

    companion object: TypedIdentifierCompanion() {
        override fun getRegistryIds(): List<ResourceLocation> {
            return Minecraft
                .getInstance().level?.registryAccess()?.registryOrThrow(Registries.BIOME)?.keySet()?.toList()
                ?: listOf()
        }
    }
}
