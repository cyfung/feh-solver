package me.kumatheta.feh.skill.assist

import me.kumatheta.feh.HeroUnit
import kotlin.math.max

object PhysicsPlus : Heal(10, isRange = true) {
    override fun healAmount(self: HeroUnit, target: HeroUnit): Int {
        val baseAmount = max(8, self.visibleStat.atk / 2)
        return healAmount(baseAmount, self, target)
    }
}