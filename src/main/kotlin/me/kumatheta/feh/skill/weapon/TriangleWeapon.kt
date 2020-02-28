package me.kumatheta.feh.skill.weapon

import me.kumatheta.feh.Axe
import me.kumatheta.feh.BasicWeapon
import me.kumatheta.feh.InCombatSkill
import me.kumatheta.feh.MapSkillMethod
import me.kumatheta.feh.Stat
import me.kumatheta.feh.WeaponType
import me.kumatheta.feh.combatSkill
import me.kumatheta.feh.skill.effect.tactics
import java.time.temporal.TemporalAmount

class TriangleWeapon(weaponType: WeaponType, might: Int) : BasicWeapon(weaponType, might) {
    override val triangleAdept: InCombatSkill<Int>? = combatSkill(20)
}

fun WeaponType.triangleAdept(might: Int): TriangleWeapon {
    return TriangleWeapon(this, might)
}