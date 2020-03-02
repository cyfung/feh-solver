package me.kumatheta.feh.skill.weapon

import me.kumatheta.feh.BattleState
import me.kumatheta.feh.DragonR
import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.skill.BasicSkill
import me.kumatheta.feh.skill.BasicWeapon
import me.kumatheta.feh.skill.inCombatSkillTrue
import me.kumatheta.feh.skill.weaponStat

val SublimeSurge = BasicWeapon(
    DragonR, BasicSkill(
        extraStat = weaponStat(16),
        adaptiveDamage = DragonAdaptive.adaptiveDamage!!,
        counterIgnoreRange = inCombatSkillTrue,
        neutralizeEffectiveWeaponType = setOf(DragonR)
    )
)