package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import liltojustice.trueadaptivemusic.client.identifier.StructureIdentifier
import liltojustice.trueadaptivemusic.client.trigger.predicate.MusicPredicate
import net.minecraft.client.Minecraft
import net.minecraft.core.registries.Registries
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.levelgen.structure.StructureStart
import net.minecraft.world.level.levelgen.structure.BoundingBox
import net.minecraft.core.BlockPos
import net.minecraft.core.SectionPos
import net.minecraft.world.level.StructureManager
import net.minecraft.world.level.levelgen.structure.Structure
import kotlin.math.max
import kotlin.math.min

class StructurePredicate internal constructor(private val structures: List<StructureIdentifier>): MusicPredicate() {
    private fun fullStructureTest(world: ServerLevel, x: Double, y: Double, z: Double): Boolean {
        val blockPos = BlockPos.containing(x, y, z)
        val structureManager = world.structureManager()
        val structureRegistry = structureManager.registryAccess().registryOrThrow(Registries.STRUCTURE)

        return (structures.takeIf { structures.isNotEmpty() }?.map { structure -> structure.id }
            ?: StructureIdentifier.getRegistryIds())
            .any { structureId ->
                val structure: Structure =
                    structureRegistry.get(structureId) ?: return false

                testStructure(structureManager, structure, blockPos)
            }
    }

    override fun getTickRate(): Int {
        return super.getTickRate() * 2
    }

    override fun test(): Boolean {
        val client = Minecraft.getInstance()
        val clientWorld = client.level ?: return false
        val serverWorld = client.singleplayerServer?.getLevel(clientWorld.dimension()) ?: return false
        val x: Double = client.player?.x ?: return false
        val y: Double = client.player?.y ?: return false
        val z: Double = client.player?.z ?: return false

        val pos = BlockPos.containing(x, y, z)
        return serverWorld.isInWorldBounds(pos) && fullStructureTest(serverWorld, x, y, z)
    }

    companion object: MusicPredicateCompanion {
        override val argDescriptions: Map<String, String>
            get() = super.argDescriptions + mapOf(
                "structures" to "Which structures the player must be in for the music to play. If none, any " +
                        "structure will trigger the music."
            )

        fun testStructure(structureAccessor: StructureManager, structure: Structure, blockPos: BlockPos): Boolean {
            var minX = Int.MAX_VALUE
            var minY = Int.MAX_VALUE
            var minZ = Int.MAX_VALUE
            var maxX = Int.MIN_VALUE
            var maxY = Int.MIN_VALUE
            var maxZ = Int.MIN_VALUE

            val structureStarts = structureAccessor.startsForStructure(SectionPos.of(blockPos), structure)
            if (structureStarts.isEmpty())
            {
                return false
            }

            for (structureStart: StructureStart in structureStarts) {
                val box = structureStart.boundingBox
                minX = min(minX, box.minX())
                minY = min(minY, box.minY())
                minZ = min(minZ, box.minZ())
                maxX = max(maxX, box.maxX())
                maxY = max(maxY, box.maxY())
                maxZ = max(maxZ, box.maxZ())
            }

            return BoundingBox(minX, minY, minZ, maxX, maxY, maxZ).inflatedBy(20).isInside(blockPos)
        }
    }
}
