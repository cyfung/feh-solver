package me.kumatheta.feh.skill.effect.incombatstat

import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.Stat
import me.kumatheta.feh.skill.CombatStatus
import me.kumatheta.feh.skill.adjacentAllies
import me.kumatheta.feh.skill.effect.InCombatStatEffect

class Brazen(private val stat: Stat) : InCombatStatEffect {
    override fun apply(combatStatus: CombatStatus<HeroUnit>): Stat {
        return if (combatStatus.self.hpThreshold(80) <= 0) {
            stat
        } else {
            Stat.ZERO
        }
    }
}

fun brazen(
    atk: Int = 0,
    spd: Int = 0,
    def: Int = 0,
    res: Int = 0
) = Brazen(Stat(atk = atk, spd = spd, def = def, res = res))