package me.kumatheta.feh.skill.effect.incombatstat

import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.Stat
import me.kumatheta.feh.skill.CombatStatus
import me.kumatheta.feh.skill.effect.InCombatStatEffect

class RangeDefStat(private val stat: Stat): InCombatStatEffect {
    override fun apply(combatStatus: CombatStatus<HeroUnit>): Stat {
        return if (!combatStatus.initAttack && combatStatus.foe.weaponType.isRanged) {
            stat
        } else {
            Stat.ZERO
        }
    }
}

fun rangeDefStat(atk: Int = 0,
             spd: Int = 0,
             def: Int = 0,
             res: Int = 0
) = RangeDefStat(Stat(atk = atk, spd = spd, def = def, res = res))