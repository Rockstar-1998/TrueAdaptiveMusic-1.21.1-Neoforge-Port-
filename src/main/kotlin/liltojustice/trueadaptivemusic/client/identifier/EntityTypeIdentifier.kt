package liltojustice.trueadaptivemusic.client.identifier

import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation

class EntityTypeIdentifier(id: String): TypedIdentifier(id) {
    companion object: TypedIdentifierCompanion<EntityTypeIdentifier>() {
        override fun getRegistryIds(): List<ResourceLocation> {
            return BuiltInRegistries.ENTITY_TYPE.keySet().toList()
        }
    }
}