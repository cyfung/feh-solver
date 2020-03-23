package me.kumatheta.feh.skill.effect.incombatstat

import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.Stat
import me.kumatheta.feh.skill.CombatStatus
import me.kumatheta.feh.skill.adjacentAllies
import me.kumatheta.feh.skill.effect.InCombatStatEffect
import me.kumatheta.feh.skill.effect.postcombat.Seal

class BondSolo(private val bond: Stat, private val solo: Stat) : InCombatStatEffect {
    override fun apply(combatStatus: CombatStatus<HeroUnit>): Stat {
        val (battleState, self) = combatStatus
        return if (self.adjacentAllies(battleState).any()) {
            bond
        } else {
            solo
        }
    }
}

fun bond(buff: Stat) = BondSolo(bond = buff, solo = Stat.ZERO)
fun bond(atk: Int = 0,
         spd: Int = 0,
         def: Int = 0,
         res: Int = 0
) = bond(Stat(atk = atk, spd = spd, def = def, res = res))

fun solo(buff: Stat) = BondSolo(bond = Stat.ZERO, solo = buff)
fun solo(atk: Int = 0,
         spd: Int = 0,
         def: Int = 0,
         res: Int = 0
) = solo(Stat(atk = atk, spd = spd, def = def, res = res))