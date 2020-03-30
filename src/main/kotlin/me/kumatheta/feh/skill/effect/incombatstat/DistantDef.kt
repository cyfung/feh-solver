package me.kumatheta.feh.skill.effect.incombatstat

import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.Stat
import me.kumatheta.feh.skill.CombatStatus
import me.kumatheta.feh.skill.effect.InCombatStatEffect

class CloseDistantDef(private val isRanged: Boolean, private val stat: Stat): InCombatStatEffect {
    override fun apply(combatStatus: CombatStatus<HeroUnit>): Stat {
        return if (!combatStatus.initAttack && combatStatus.foe.weaponType.isRanged == isRanged) {
            stat
        } else {
            Stat.ZERO
        }
    }
}

fun distantDef(atk: Int = 0,
               spd: Int = 0,
               def: Int = 0,
               res: Int = 0
) = CloseDistantDef(true, Stat(atk = atk, spd = spd, def = def, res = res))

fun closeDef(atk: Int = 0,
               spd: Int = 0,
               def: Int = 0,
               res: Int = 0
) = CloseDistantDef(false, Stat(atk = atk, spd = spd, def = def, res = res))