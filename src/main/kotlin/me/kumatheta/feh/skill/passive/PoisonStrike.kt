package me.kumatheta.feh.skill.passive

import me.kumatheta.feh.BattleState
import me.kumatheta.feh.CombatEndSkill
import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.InCombatStat
import me.kumatheta.feh.Passive

object PoisonStrike3 : Passive {
    override val combatEnd: CombatEndSkill? = object : CombatEndSkill {
        override fun apply(
            battleState: BattleState,
            self: InCombatStat,
            foe: InCombatStat,
            attack: Boolean,
            attacked: Boolean
        ) {
            if (attack) {
                foe.heroUnit.endOfCombatEffects.takeNonLethalDamage(10)
            }
        }
    }
}