package me.kumatheta.feh.skill.effect

import me.kumatheta.feh.MoveType
import me.kumatheta.feh.WeaponType

class EffectiveAgainstWeapon(val weaponType: WeaponType) : SkillEffect

class EffectiveAgainstMovement(val moveType: MoveType) : SkillEffect

class NeutralizeEffectiveAgainstWeapon(val weaponType: WeaponType) : SkillEffect

class NeutralizeEffectiveAgainstMovement(val moveType: MoveType) : SkillEffect