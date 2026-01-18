package liltojustice.trueadaptivemusic.client.identifier

import net.minecraft.client.Minecraft
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceLocation

class StructureIdentifier(id: String): TypedIdentifier(id) {
    companion object: TypedIdentifierCompanion<StructureIdentifier>() {
        override fun getRegistryIds(): List<ResourceLocation> {
            return Minecraft.getInstance().level
                ?.registryAccess()
                ?.registryOrThrow(Registries.STRUCTURE)
                ?.keySet()
                ?.toList()
                ?: emptyList()
        }
    }
}