package me.kumatheta.feh.skill.passive

import me.kumatheta.feh.BattleState
import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.skill.CombatStatus
import me.kumatheta.feh.skill.InCombatStat
import me.kumatheta.feh.skill.effect.FlatDamageReduce
import me.kumatheta.feh.skill.effect.StartOfTurnEffect
import me.kumatheta.feh.skill.toSkill

val ShieldPulse3 = sequenceOf(
    object : StartOfTurnEffect {
        override fun onStartOfTurn(battleState: BattleState, self: HeroUnit) {
            if (battleState.turn == 1) {
                self.reduceCooldown(2)
            }
        }
    },
    object : FlatDamageReduce {
        override fun getDamageReduce(combatStatus: CombatStatus<InCombatStat>, specialTriggered: Boolean): Int {
            return if (specialTriggered) {
                5
            } else {
                0
            }
        }
    }
).toSkill()
