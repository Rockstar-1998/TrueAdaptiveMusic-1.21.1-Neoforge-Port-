package liltojustice.trueadaptivemusic.client.identifier

import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation

class EntityTypeIdentifier(id: ResourceLocation): TypedIdentifier(id) {
    override fun toPrefixedTranslationKey(): String {
        return id.toLanguageKey("entity")
    }

    companion object: TypedIdentifierCompanion() {
        override fun getRegistryIds(): List<ResourceLocation> {
            return BuiltInRegistries.ENTITY_TYPE.keySet().toList()
        }
    }
}
