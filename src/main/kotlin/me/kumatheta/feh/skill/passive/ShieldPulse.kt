package me.kumatheta.feh.skill.passive

import me.kumatheta.feh.BattleState
import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.InCombatStat
import me.kumatheta.feh.MapSkillMethod
import me.kumatheta.feh.Passive
import me.kumatheta.feh.PerAttackSkill

object ShieldPulse3 : Passive {
    override val startOfTurn: MapSkillMethod<Unit>? = object : MapSkillMethod<Unit> {
        override fun apply(battleState: BattleState, self: HeroUnit) {
            if (battleState.turn == 1) {
                self.reduceCooldown(2)
            }
        }
    }

    override val flatDamageReduce: PerAttackSkill<Int>? = object : PerAttackSkill<Int> {
        override fun apply(
            battleState: BattleState,
            self: InCombatStat,
            foe: InCombatStat,
            specialTriggered: Boolean
        ): Int {
            return if (specialTriggered) {
                5
            } else {
                0
            }
        }
    }
}