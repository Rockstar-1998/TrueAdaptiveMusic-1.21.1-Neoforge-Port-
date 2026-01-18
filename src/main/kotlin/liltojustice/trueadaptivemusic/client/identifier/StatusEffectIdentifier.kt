package liltojustice.trueadaptivemusic.client.identifier

import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation

class StatusEffectIdentifier(id: String): TypedIdentifier(id) {
    companion object: TypedIdentifierCompanion<StatusEffectIdentifier>() {
        override fun getRegistryIds(): List<ResourceLocation> {
            return BuiltInRegistries.MOB_EFFECT.keySet().toList()
        }
    }
}