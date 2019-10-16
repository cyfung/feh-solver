package com.bloombase.feh.skill

import com.bloombase.feh.BattleState
import com.bloombase.feh.HeroUnit
import com.bloombase.feh.CombatSkillMethod
import com.bloombase.feh.Stat

class Seal(private val stat: Stat) : CombatSkillMethod<Unit> {

    override fun apply(battleState: BattleState, self: HeroUnit, opponent: HeroUnit, attack: Boolean) {
        if(!self.isDead && !opponent.isDead) {
            opponent.applyDebuff(stat)
        }
    }
}