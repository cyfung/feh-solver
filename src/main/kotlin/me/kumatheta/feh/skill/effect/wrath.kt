package me.kumatheta.feh.skill.effect

import me.kumatheta.feh.*
import me.kumatheta.feh.skill.BasicSkill
import me.kumatheta.feh.skill.CombatStatus
import me.kumatheta.feh.skill.InCombatStat
import me.kumatheta.feh.skill.MapSkillMethod

fun wrathDamage(hpPercentage: Int): ((CombatStatus<InCombatStat>, specialTriggered: Boolean) -> Int)? = { combatStatus, specialTriggered ->
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

fun wrath(hpPercentage: Int)= BasicSkill(
    startOfTurn = wrathSpecialCharge(hpPercentage),
    damageIncrease = wrathDamage(hpPercentage)
)