package me.kumatheta.feh.skill.effect

import me.kumatheta.feh.BattleState
import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.MapSkillMethod
import me.kumatheta.feh.PerAttackSkill

fun wrathDamage(hpPercentage: Int): PerAttackSkill<Int>? = { combatStatus, specialTriggered ->
    if (specialTriggered && combatStatus.self.heroUnit.hpThreshold(hpPercentage) <= 0) {
        10
    } else {
        0
    }
}

fun wrathSpecialCharge(hpPercentage: Int): MapSkillMethod<Unit> = { _: BattleState, self: HeroUnit ->
    if (self.hpThreshold(hpPercentage) <= 0) {
        self.cachedEffect.cooldown--
    }
}