package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import liltojustice.trueadaptivemusic.client.identifier.StructureIdentifier
import liltojustice.trueadaptivemusic.client.trigger.predicate.MusicPredicate
import net.minecraft.client.Minecraft
import net.minecraft.core.BlockPos
import net.minecraft.core.SectionPos
import net.minecraft.core.registries.Registries
import net.minecraft.server.level.ServerLevel
import net.minecraft.util.GsonHelper
import net.minecraft.world.level.StructureManager
import net.minecraft.world.level.levelgen.structure.Structure
import net.minecraft.world.level.levelgen.structure.StructureStart
import net.minecraft.world.level.levelgen.structure.BoundingBox
import kotlin.math.max
import kotlin.math.min

class StructurePredicate internal constructor(private val structures: List<StructureIdentifier>): MusicPredicate() {
    private fun fullStructureTest(world: ServerLevel, x: Double, y: Double, z: Double): Boolean {
        val blockPos = BlockPos.containing(x, y, z)
        val structureManager = world.structureManager()

        return (structures.takeIf { structures.isNotEmpty() }?.map { structure -> structure.identifier }
            ?: StructureIdentifier.getRegistryIds())
            .any { structureId ->
                val structure: Structure =
                    world.registryAccess().registryOrThrow(Registries.STRUCTURE).get(structureId) ?: return false

                testStructure(structureManager, structure, blockPos)
            }
    }

    override fun getTickRate(): Int {
        return 10
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

    override fun toJson(): JsonObject {
        val result = JsonObject()
        val jsonStructures = JsonArray()
        structures.forEach { structure -> jsonStructures.add(structure.toString()) }
        result.add("id", jsonStructures)

        return result
    }

    companion object: MusicPredicateCompanion<StructurePredicate> {
        override fun fromJson(json: JsonObject): StructurePredicate {
            return StructurePredicate(
                if (json.has("id") && json.get("id").isJsonArray)
                    json.getAsJsonArray("id").map { element -> StructureIdentifier(element.asString) }
                else
                    listOf(StructureIdentifier(GsonHelper.getAsString(json, "id"))))
        }

        fun testStructure(structureManager: StructureManager, structure: Structure, blockPos: BlockPos): Boolean {
            var minX = Int.MAX_VALUE
            var minY = Int.MAX_VALUE
            var minZ = Int.MAX_VALUE
            var maxX = Int.MIN_VALUE
            var maxY = Int.MIN_VALUE
            var maxZ = Int.MIN_VALUE

            val structureStarts = structureManager.startsForStructure(SectionPos.of(blockPos), structure)
            if (structureStarts.isEmpty())
            {
                return false
            }

            for (structureStart: StructureStart in structureStarts) {
                minX = min(minX, structureStart.boundingBox.minX())
                minY = min(minY, structureStart.boundingBox.minY())
                minZ = min(minZ, structureStart.boundingBox.minZ())
                maxX = max(maxX, structureStart.boundingBox.maxX())
                maxY = max(maxY, structureStart.boundingBox.maxY())
                maxZ = max(maxZ, structureStart.boundingBox.maxZ())
            }

            return BoundingBox(minX, minY, minZ, maxX, maxY, maxZ).inflatedBy(20).isInside(blockPos)
        }
    }
}