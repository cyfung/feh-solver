package me.kumatheta.feh.skill.passive

import me.kumatheta.feh.BattleState
import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.skill.CombatStatus
import me.kumatheta.feh.skill.InCombatStat
import me.kumatheta.feh.skill.effect.DamageIncrease
import me.kumatheta.feh.skill.effect.StartOfTurnEffect
import me.kumatheta.feh.skill.toSkill

class WrathDamage(private val hpPercentage: Int) : DamageIncrease {
    override fun getDamageIncrease(combatStatus: CombatStatus<InCombatStat>, specialTriggered: Boolean): Int {
        return if (specialTriggered && combatStatus.self.heroUnit.hpThreshold(hpPercentage) <= 0) {
            10
        } else {
            0
        }
    }
}

class WrathSpecialCharge(private val hpPercentage: Int) :
    StartOfTurnEffect {
    override fun onStartOfTurn(battleState: BattleState, self: HeroUnit) {
        if (self.hpThreshold(hpPercentage) <= 0) {
            self.cachedEffect.cooldown--
        }
    }
}

fun wrath(hpPercentage: Int) = sequenceOf(
    WrathSpecialCharge(hpPercentage),
    WrathDamage(hpPercentage)
).toSkill()