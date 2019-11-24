package me.kumatheta.feh.skill

import me.kumatheta.feh.BattleState
import me.kumatheta.feh.CombatStartSkill
import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.Stat

class BlowOrStance(private val blow: Stat, private val stance: Stat) :
    CombatStartSkill<Stat> {
    override fun apply(battleState: BattleState, self: HeroUnit, foe: HeroUnit, initAttack: Boolean): Stat {
        return if (initAttack) {
            blow
        } else {
            stance
        }
    }

}