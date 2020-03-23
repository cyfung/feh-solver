package me.kumatheta.feh.skill.effect.incombatstat

import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.Stat
import me.kumatheta.feh.skill.CombatStatus
import me.kumatheta.feh.skill.adjacentAllies
import me.kumatheta.feh.skill.effect.InCombatStatEffect

object OwlEffect : InCombatStatEffect {
    override fun apply(combatStatus: CombatStatus<HeroUnit>): Stat {
        val (battleState, self) = combatStatus
        val buff = self.adjacentAllies(battleState).count() * 2
        return Stat(atk = buff, spd = buff, def = buff, res = buff)
    }
}
