package me.kumatheta.feh.skill.weapon

import me.kumatheta.feh.*

object Ragnell : Weapon(Sword) {
    override val counterIgnoreRange: InCombatSkill<Boolean>?
        get() = inCombatSkillTrue
}