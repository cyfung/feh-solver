package me.kumatheta.feh.skill.weapon

import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.MagicR
import me.kumatheta.feh.Stat
import me.kumatheta.feh.skill.CombatStatus
import me.kumatheta.feh.skill.basic
import me.kumatheta.feh.skill.effect.InCombatStatEffect
import me.kumatheta.feh.skill.plus

private val IN_COMBAT_BUFF = Stat(atk = 3, spd = 3)

val Gleipnir = MagicR.basic(14, res = 3) + object : InCombatStatEffect {
    override fun apply(combatStatus: CombatStatus<HeroUnit>): Stat {
        return if (combatStatus.foe.currentHp == combatStatus.foe.maxHp) {
            IN_COMBAT_BUFF
        } else {
            Stat.ZERO
        }
    }
}