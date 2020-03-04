package me.kumatheta.feh.skill.weapon

import me.kumatheta.feh.WeaponType
import me.kumatheta.feh.skill.BasicSkill
import me.kumatheta.feh.skill.BasicWeapon
import me.kumatheta.feh.skill.Weapon

class EmptyWeapon private constructor(basicWeapon: BasicWeapon) : Weapon by basicWeapon {
    constructor(weaponType: WeaponType) : this(BasicWeapon(weaponType, BasicSkill()))
}