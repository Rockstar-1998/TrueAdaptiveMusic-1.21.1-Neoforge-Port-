package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import liltojustice.trueadaptivemusic.client.identifier.StructureSetIdentifier
import liltojustice.trueadaptivemusic.client.trigger.predicate.MusicPredicate
import net.minecraft.client.Minecraft
import net.minecraft.core.BlockPos
import net.minecraft.core.registries.Registries
import net.minecraft.server.level.ServerLevel
import net.minecraft.util.GsonHelper
import net.minecraft.world.level.levelgen.structure.StructureSet

class StructureSetPredicate internal constructor(private val structureSets: List<StructureSetIdentifier>): MusicPredicate() {
    private fun fullStructureTest(world: ServerLevel, x: Double, y: Double, z: Double): Boolean {
        val blockPos = BlockPos.containing(x, y, z)
        val structureManager = world.structureManager()

        return (structureSets.takeIf { structureSets.isNotEmpty() }?.map { structureSet -> structureSet.identifier }
            ?: StructureSetIdentifier.getRegistryIds())
            .any { structureSetId ->
                val structureSet: StructureSet =
                    world.registryAccess().registryOrThrow(Registries.STRUCTURE_SET).get(structureSetId) ?: return false

                structureSet.structures().any { structureWeightedEntry ->
                    StructurePredicate.testStructure(structureManager, structureWeightedEntry.structure().value(), blockPos) }
            }
    }

    override fun test(minecraft: Minecraft): Boolean {
        val server = minecraft.singleplayerServer ?: return false
        val serverWorld = server.allLevels.firstOrNull { world ->
            world.dimension() == minecraft.level?.dimension() } ?: return false
        val x: Double = minecraft.player?.x ?: return false
        val y: Double = minecraft.player?.y ?: return false
        val z: Double = minecraft.player?.z ?: return false

        return serverWorld.isLoaded(BlockPos.containing(x, y, z)) && fullStructureTest(serverWorld, x, y, z)
    }

    override fun getTickRate(): Int {
        return 10
    }

    override fun toJson(): JsonObject {
        val result = JsonObject()
        val jsonStructureSets = JsonArray()
        structureSets.forEach { structureSet -> jsonStructureSets.add(structureSet.toString()) }
        result.add("id", jsonStructureSets)

        return result
    }

    companion object: MusicPredicateCompanion<StructureSetPredicate> {
        override fun fromJson(json: JsonObject): StructureSetPredicate {
            return StructureSetPredicate(
                if (json.has("id") && json.get("id").isJsonArray)
                    json.getAsJsonArray("id").map { element -> StructureSetIdentifier(element.asString) }
                else
                    listOf(StructureSetIdentifier(GsonHelper.getAsString(json, "id"))))
        }
    }
}