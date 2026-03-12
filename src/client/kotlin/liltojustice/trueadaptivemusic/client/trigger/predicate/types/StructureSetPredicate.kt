package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import liltojustice.trueadaptivemusic.client.identifier.StructureSetIdentifier
import liltojustice.trueadaptivemusic.client.trigger.predicate.MusicPredicate
import net.minecraft.client.Minecraft
import net.minecraft.core.registries.Registries
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.levelgen.structure.StructureSet
import net.minecraft.core.BlockPos
import kotlin.jvm.optionals.getOrNull

class StructureSetPredicate internal constructor(
    private val structureSets: List<StructureSetIdentifier>): MusicPredicate() {

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

    override fun getTickRate(): Int {
        return super.getTickRate() * 2
    }

    private fun fullStructureTest(world: ServerLevel, x: Double, y: Double, z: Double): Boolean {
        val blockPos = BlockPos.containing(x, y, z)
        val structureManager = world.structureManager()
        val structureSetRegistry = structureManager.registryAccess().registryOrThrow(Registries.STRUCTURE_SET)

        return (structureSets.takeIf { structureSets.isNotEmpty() }?.map { structureSet -> structureSet.id }
            ?: StructureSetIdentifier.getRegistryIds())
            .any { structureSetId ->
                val structureSet: StructureSet =
                    structureSetRegistry.get(structureSetId) ?: return false

                structureSet.structures().any { structureWeightedEntry ->
                    StructurePredicate.testStructure(
                        structureManager, structureWeightedEntry.structure().value(), blockPos) }
            }
    }

    companion object: MusicPredicateCompanion {
        override val argDescriptions: Map<String, String>
            get() = super.argDescriptions + mapOf(
                "structureSets" to "Which structure sets the player must be in for the music should play. If none, " +
                        "any structure set will trigger the music."
            )
    }
}
