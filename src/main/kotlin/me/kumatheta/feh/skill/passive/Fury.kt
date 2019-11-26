package me.kumatheta.feh.skill.passive

import me.kumatheta.feh.BattleState
import me.kumatheta.feh.CombatEndSkill
import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.Passive
import me.kumatheta.feh.Stat

object Fury3 : Passive {
    override val extraStat: Stat? = Stat(atk = 3, spd = 3, def = 3, res = 3)

    override val combatEnd: CombatEndSkill? = object : CombatEndSkill {
        override fun apply(
            battleState: BattleState,
            self: HeroUnit,
            foe: HeroUnit,
            attack: Boolean,
            attacked: Boolean
        ) {
            self.endOfCombatEffects.takeNonLethalDamage(6)
        }
    }
}