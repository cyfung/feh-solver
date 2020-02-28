package me.kumatheta.feh.skill.weapon

import me.kumatheta.feh.BasicWeapon
import me.kumatheta.feh.DragonR
import me.kumatheta.feh.InCombatSkill
import me.kumatheta.feh.MapSkillMethod
import me.kumatheta.feh.WeaponType
import me.kumatheta.feh.inCombatSkillTrue

object SublimeSurge : BasicWeapon(DragonR, 16) {
    override val adaptiveDamage: MapSkillMethod<Boolean>? = { _, _ ->
        true
    }
    override val counterIgnoreRange: InCombatSkill<Boolean>?
        get() = inCombatSkillTrue
    override val neutralizeEffectiveWeaponType: Set<WeaponType>? = setOf(DragonR)
}