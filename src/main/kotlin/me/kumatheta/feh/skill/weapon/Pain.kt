package me.kumatheta.feh.skill.weapon

import me.kumatheta.feh.BasicWeapon
import me.kumatheta.feh.BattleState
import me.kumatheta.feh.CombatEndSkill
import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.InCombatStat
import me.kumatheta.feh.Staff

object Pain : BasicWeapon(Staff, 3) {
    override val combatEnd: CombatEndSkill? = object : CombatEndSkill {
        override fun apply(
            battleState: BattleState,
            self: InCombatStat,
            foe: InCombatStat,
            attack: Boolean,
            attacked: Boolean
        ) {
            if (attacked && !foe.heroUnit.isDead) {
                foe.heroUnit.endOfCombatEffects.takeNonLethalDamage(10)
            }
        }
    }
}