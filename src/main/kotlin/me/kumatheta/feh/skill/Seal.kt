package me.kumatheta.feh.skill

import me.kumatheta.feh.BattleState
import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.CombatSkillMethod
import me.kumatheta.feh.Stat

class Seal(private val stat: Stat) : CombatSkillMethod<Unit> {

    override fun apply(battleState: BattleState, self: HeroUnit, foe: HeroUnit, attack: Boolean) {
        if(!self.isDead && !foe.isDead) {
            foe.applyDebuff(stat)
        }
    }
}