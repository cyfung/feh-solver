package me.kumatheta.feh.skill.passive

import me.kumatheta.feh.BattleState
import me.kumatheta.feh.InCombatSkill
import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.InCombatStatus
import me.kumatheta.feh.Passive

object QuickRiposte3 : Passive {
    override val followUp: InCombatSkill<Int>? = QRMethod(70)
}

object QuickRiposte2 : Passive {
    override val followUp: InCombatSkill<Int>? = QRMethod(80)
}

object QuickRiposte1 : Passive {
    override val followUp: InCombatSkill<Int>? = QRMethod(90)
}

class QRMethod(private val percentage: Int) : InCombatSkill<Int> {
    override fun apply(battleState: BattleState, self: InCombatStatus, foe: InCombatStatus, initAttack: Boolean): Int {
        return if (self.heroUnit.hpThreshold(percentage) >= 0) {
            1
        } else {
            0
        }
    }
}