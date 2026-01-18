package liltojustice.trueadaptivemusic.client.identifier

import net.minecraft.client.Minecraft
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceLocation

class BiomeIdentifier(id: String): TypedIdentifier(id) {
    companion object: TypedIdentifierCompanion<BiomeIdentifier>() {
        override fun getRegistryIds(): List<ResourceLocation> {
            return Minecraft
                .getInstance().level?.registryAccess()?.registryOrThrow(Registries.BIOME)?.keySet()?.toList() ?: listOf()
        }
    }
}