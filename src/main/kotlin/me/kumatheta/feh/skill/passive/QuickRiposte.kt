package me.kumatheta.feh.skill.passive

import me.kumatheta.feh.BattleState
import me.kumatheta.feh.CombatSkillMethod
import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.Passive

object QuickRiposte3 : Passive {
    override val followUpSelf: CombatSkillMethod<Int>? = QRMethod(70)
}

object QuickRiposte2 : Passive {
    override val followUpSelf: CombatSkillMethod<Int>? = QRMethod(80)
}

object QuickRiposte1 : Passive {
    override val followUpSelf: CombatSkillMethod<Int>? = QRMethod(90)
}

class QRMethod(private val percentage: Int) : CombatSkillMethod<Int> {
    override fun apply(battleState: BattleState, self: HeroUnit, foe: HeroUnit, attack: Boolean): Int {
        return if (self.hpThreshold(percentage) >= 0) {
            1
        } else {
            0
        }
    }
}