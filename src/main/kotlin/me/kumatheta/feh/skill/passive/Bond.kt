package me.kumatheta.feh.skill.passive

import me.kumatheta.feh.BattleState
import me.kumatheta.feh.CombatStartSkill
import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.Passive
import me.kumatheta.feh.Stat

class Bond(val stat: Stat) : CombatStartSkill<Stat> {
    override fun apply(battleState: BattleState, self: HeroUnit, foe: HeroUnit, initAttack: Boolean): Stat {
        return if (battleState.unitsSeq(self.team).filterNot { it == self }.any { it.position.distanceTo(self.position) == 1 }) {
            stat
        } else {
            Stat.ZERO
        }
    }
}
