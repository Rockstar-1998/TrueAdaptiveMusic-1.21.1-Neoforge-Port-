package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import com.google.gson.JsonObject
import liltojustice.trueadaptivemusic.client.trigger.predicate.MusicPredicate
import net.minecraft.client.Minecraft
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.Mob
import net.minecraft.world.phys.Vec3
import java.util.*
import kotlin.concurrent.schedule
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.cbrt
import kotlin.math.tan

class CombatPredicate: MusicPredicate() {
    private val aggroTimer: Timer = Timer()
    private var aggroTimerTask: TimerTask? = null
    private var isAggro: Boolean = false

    override fun test(minecraft: Minecraft): Boolean {
        val playerEntity = minecraft.player ?: return false
        val world = minecraft.level ?: return false
        val fovValue = minecraft.options.fov().get()
        val verticalFov = fovValue.toDouble() / DEG_PER_RAD
        val horizontalFov = 2 * kotlin.math.atan(tan(verticalFov / 2) * minecraft.window.width / minecraft.window.height)
        val viewVec = playerEntity.getViewVector(1.0f)
        val verticalAngle = acos(viewVec.y)
        val horizontalAngle = acos(viewVec.x)

        for (entity: Entity in world.entitiesForRendering())
        {
            val mobEntity: Mob = entity as? Mob ?: continue
            val relativeMobEntityPosN = mobEntity.position().subtract(playerEntity.position()).normalize()

            val mobVerticalAngle = acos(relativeMobEntityPosN.y)
            val mobHorizontalAngle = acos(relativeMobEntityPosN.x)

            if (!isAggro && (abs(mobVerticalAngle - verticalAngle) > verticalFov / 2
                        || abs(mobHorizontalAngle - horizontalAngle) > horizontalFov / 2)) {
                continue
            }

            if (mobEntity.target?.id == playerEntity.id
                || (mobEntity.isAggressive
                        && closeEnough(
                    relativeMobEntityPosN,
                    Vec3(mobEntity.boundingBox.xsize,
                        mobEntity.boundingBox.ysize,
                        mobEntity.boundingBox.zsize))))
            {
                isAggro = true
                aggroTimerTask?.cancel()
                aggroTimerTask = aggroTimer.schedule(1000L * AGGRO_TIMER_SECONDS) {
                    isAggro = false
                    aggroTimerTask = null
                }

                return true
            }
        }

        return isAggro
    }

    override fun getTickRate(): Int {
        return 10
    }

    companion object: MusicPredicateCompanion<CombatPredicate> {
        override fun fromJson(json: JsonObject): CombatPredicate {
            return CombatPredicate()
        }

        private val baseAxialDistance = Vec3(20.0, 20.0, 20.0)
        private const val AGGRO_TIMER_SECONDS = 2L
        private const val DEG_PER_RAD = 180.0 / PI

        fun closeEnough(displacement: Vec3, attackerSize: Vec3): Boolean
        {
            val axialDistance = Vec3(abs(displacement.x), abs(displacement.y), abs(displacement.z))
            val scaledAttackerMinDistance = baseAxialDistance
                .multiply(cbrt(attackerSize.x), cbrt(attackerSize.y), cbrt(attackerSize.z))
            return axialDistance.x < scaledAttackerMinDistance.x
                    && axialDistance.y < scaledAttackerMinDistance.y
                    && axialDistance.z < scaledAttackerMinDistance.z
        }
    }
}