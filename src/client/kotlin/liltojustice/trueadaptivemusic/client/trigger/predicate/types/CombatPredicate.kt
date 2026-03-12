package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import liltojustice.trueadaptivemusic.client.identifier.EntityTypeIdentifier
import liltojustice.trueadaptivemusic.client.trigger.predicate.MusicPredicate
import net.minecraft.client.Minecraft
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.monster.Guardian
import net.minecraft.world.entity.monster.Monster
import net.minecraft.world.entity.Mob
import net.minecraft.world.entity.monster.Phantom
import net.minecraft.world.entity.player.Player
import net.minecraft.world.phys.Vec3
import java.util.*
import kotlin.concurrent.schedule
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.atan
import kotlin.math.cbrt
import kotlin.math.tan

class CombatPredicate(
    private val blacklist: Boolean, private val mobEntities: List<EntityTypeIdentifier>) : MusicPredicate() {
    private val aggroTimer: Timer = Timer()
    private var aggroTimerTask: TimerTask? = null
    private var isAggro: Boolean = false
    private val mobEntityTranslationKeys = mobEntities.map { mobEntity -> mobEntity.toTranslationKey("entity") }

    override fun test(): Boolean {
        val client = Minecraft.getInstance()
        val playerEntity = client.player ?: return false
        val world = client.level ?: return false
        val verticalFov = client.options.fov().get().toDouble() / DEG_PER_RAD
        val horizontalFov = 2 * atan(tan(verticalFov / 2) * client.window.width / client.window.height)
        val viewVector = playerEntity.lookAngle
        val verticalAngle = acos(viewVector.y)
        val horizontalAngle = acos(viewVector.x)

        val entityGroups = mutableListOf<List<Mob>>()

        entityGroups.add(world.entitiesForRendering().mapNotNull { it as? Monster }.filter { filterEntity(it) })
        entityGroups.add(world.entitiesForRendering().mapNotNull { it as? Phantom }.filter { filterEntity(it) })

        for (validEntities in entityGroups) {
            for (mobEntity: Mob in validEntities) {
                if (processMob(mobEntity, playerEntity, verticalAngle, horizontalAngle, verticalFov, horizontalFov)) {
                    return true
                }
            }
        }

        return isAggro
    }

    override fun getTickRate(): Int {
        return super.getTickRate() * 2
    }

    private fun processMob(mobEntity: Mob, playerEntity: Player, verticalAngle: Double, horizontalAngle: Double, verticalFov: Double, horizontalFov: Double): Boolean {
        val relativeMobEntityPos = mobEntity.position().subtract(playerEntity.position())
        val relativeMobEntityPosN = relativeMobEntityPos.normalize()

        val mobVerticalAngle = acos(relativeMobEntityPosN.y)
        val mobHorizontalAngle = acos(relativeMobEntityPosN.x)

        if (!isAggro && (abs(mobVerticalAngle - verticalAngle) > verticalFov / 2
                    || abs(mobHorizontalAngle - horizontalAngle) > horizontalFov / 2)) {
            return false
        }

        if (isValidAttacker(mobEntity, playerEntity, relativeMobEntityPos)) {
            isAggro = true
            aggroTimerTask?.cancel()
            aggroTimerTask = aggroTimer.schedule(1000L * AGGRO_TIMER_SECONDS) {
                isAggro = false
                aggroTimerTask = null
            }

            return true
        }

        return false
    }

    private fun filterEntity(entity: Entity): Boolean {
        return mobEntityTranslationKeys
            .takeIf { it.isNotEmpty() }
            ?.let {
                if (blacklist)
                    it.none { mobEntity -> mobEntity == entity.type.descriptionId }
                else
                    it.any { mobEntity -> mobEntity == entity.type.descriptionId }
            }
            ?: true
    }

    companion object: MusicPredicateCompanion {
        private val baseAxialDistance = Vec3(20.0, 20.0, 20.0)
        private const val AGGRO_TIMER_SECONDS = 4L
        private const val DEG_PER_RAD = 180.0 / PI

        override val argDescriptions: Map<String, String>
            get() = super.argDescriptions + mapOf(
                "blacklist" to "Whether the list of mob entities attacking should not (if checked) or should " +
                        "(if not checked) make the music play.",
                "mobEntities" to "Select mob entities for this predicate. If none, any entity will trigger the music."
            )

        private fun isValidAttacker(
            mobEntity: Mob, playerEntity: Player, displacement: Vec3): Boolean {
            val closeEnough = closeEnough(
                    displacement,
                    Vec3(
                        mobEntity.boundingBox.xsize,
                        mobEntity.boundingBox.ysize,
                        mobEntity.boundingBox.zsize
                    )
            )
            return (mobEntity.isAggressive && closeEnough) ||
                    ((mobEntity as? Guardian)?.let { it.activeAttackTarget?.id == playerEntity.id } == true) ||
                    ((mobEntity is Phantom) && closeEnough)
        }

        private fun closeEnough(displacement: Vec3, attackerSize: Vec3): Boolean
        {
            val axialDistance = Vec3(
                abs(displacement.x), abs(displacement.y), abs(displacement.z))
            val scaledAttackerMinDistance = baseAxialDistance
                .multiply(Vec3(cbrt(attackerSize.x), cbrt(attackerSize.y), cbrt(attackerSize.z)))
            return axialDistance.x < scaledAttackerMinDistance.x
                    && axialDistance.y < scaledAttackerMinDistance.y
                    && axialDistance.z < scaledAttackerMinDistance.z
        }
    }
}
