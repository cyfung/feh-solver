package me.kumatheta.feh.skill

import me.kumatheta.feh.BattleState
import me.kumatheta.feh.CombatEndSkill
import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.Stat

class Seal(private val stat: Stat) : CombatEndSkill {

    override fun apply(battleState: BattleState, self: HeroUnit, foe: HeroUnit, attack: Boolean, attacked: Boolean) {
        if (!self.isDead && !foe.isDead) {
            foe.applyDebuff(stat)
        }
    }
}