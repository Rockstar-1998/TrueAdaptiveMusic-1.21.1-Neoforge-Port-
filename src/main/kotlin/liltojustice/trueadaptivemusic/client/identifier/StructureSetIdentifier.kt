package liltojustice.trueadaptivemusic.client.identifier

import net.minecraft.client.Minecraft
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceLocation

class StructureSetIdentifier(id: String): TypedIdentifier(id) {
    companion object: TypedIdentifierCompanion<StructureSetIdentifier>() {
        override fun getRegistryIds(): List<ResourceLocation> {
            return Minecraft.getInstance().level
                ?.registryAccess()
                ?.registryOrThrow(Registries.STRUCTURE_SET)
                ?.keySet()
                ?.toList()
                ?: emptyList()
        }
    }
}