package liltojustice.trueadaptivemusic.client.identifier

import net.minecraft.client.Minecraft
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceLocation

class DimensionIdentifier(id: String): TypedIdentifier(id) {
    companion object: TypedIdentifierCompanion<DimensionIdentifier>() {
        override fun getRegistryIds(): List<ResourceLocation> {
            return Minecraft
                .getInstance().level?.registryAccess()?.registryOrThrow(Registries.DIMENSION_TYPE)?.keySet()?.toList() ?: listOf()
        }
    }
}