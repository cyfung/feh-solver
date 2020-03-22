package me.kumatheta.feh.skill.effect

import me.kumatheta.feh.BattleState
import me.kumatheta.feh.HeroUnit

interface HealEffect : SkillEffect {
    fun onHeal(battleState: BattleState, self: HeroUnit, ally: HeroUnit, healAmount: Int, selfUseHeal: Boolean)
}