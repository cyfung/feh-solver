package me.kumatheta.feh.skill.passive

import me.kumatheta.feh.BattleState
import me.kumatheta.feh.HeroUnit
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

    override val flatDamageReduce: PerAttackSkill<Int>? = { _, specialTriggered ->
        if (specialTriggered) {
            5
        } else {
            0
        }
    }
}
