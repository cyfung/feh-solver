package me.kumatheta.feh.skill.effect.incombatstat

import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.Stat
import me.kumatheta.feh.skill.CombatStatus
import me.kumatheta.feh.skill.effect.InCombatStatEffect

class BlowStance(private val blow: Stat, private val stance: Stat): InCombatStatEffect {
    override fun apply(combatStatus: CombatStatus<HeroUnit>): Stat {
        return if (combatStatus.initAttack) {
            blow
        } else {
            stance
        }
    }
}

fun blow(stat: Stat) = BlowStance(stat, Stat.ZERO)
fun blow(atk: Int = 0,
         spd: Int = 0,
         def: Int = 0,
         res: Int = 0
) = blow(Stat(atk = atk, spd = spd, def = def, res = res))

fun stance(stat: Stat) = BlowStance(Stat.ZERO, stat)
fun stance(atk: Int = 0,
         spd: Int = 0,
         def: Int = 0,
         res: Int = 0
) = stance(Stat(atk = atk, spd = spd, def = def, res = res))