package me.kumatheta.feh.skill.assist

import me.kumatheta.feh.HeroUnit
import kotlin.math.max

class HealingPlus(threshold: Int, isRange: Boolean, private val healModifier: Int, private val minHeal: Int) :
    Heal(threshold, isRange) {
    override fun healAmount(self: HeroUnit, target: HeroUnit): Int {
        val baseAmount = max(minHeal, self.visibleStat.atk / 2 + healModifier)
        return healAmount(baseAmount, self, target)
    }
}