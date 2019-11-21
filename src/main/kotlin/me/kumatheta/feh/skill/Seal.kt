package me.kumatheta.feh.skill

import me.kumatheta.feh.*

class Seal(private val stat: Stat) : CombatEndSkillMethod {

    override fun apply(battleState: BattleState, self: HeroUnit, foe: HeroUnit, attack: Boolean, attacked: Boolean) {
        if(!self.isDead && !foe.isDead) {
            foe.applyDebuff(stat)
        }
    }
}