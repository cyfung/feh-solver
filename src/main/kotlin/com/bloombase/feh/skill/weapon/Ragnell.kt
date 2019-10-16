package com.bloombase.feh.skill.weapon

import com.bloombase.feh.*

object Ragnell : Weapon(Sword) {
    override val ignoreRange: CombatSkillMethod<Boolean>?
        get() = CombatSkillMethodTrue
}