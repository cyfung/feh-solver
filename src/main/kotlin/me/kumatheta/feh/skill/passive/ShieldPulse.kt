package me.kumatheta.feh.skill.passive

import me.kumatheta.feh.*
import me.kumatheta.feh.skill.*

object ShieldPulse3 : BasicSkill() {
    override val startOfTurn: MapSkillMethod<Unit>? = { battleState: BattleState, self: HeroUnit ->
        if (battleState.turn == 1) {
            self.reduceCooldown(2)
        }
    }

    override val flatDamageReduce: ((CombatStatus<InCombatStat>, specialTriggered: Boolean) -> Int)? = { _, specialTriggered ->
        if (specialTriggered) {
            5
        } else {
            0
        }
    }
}
