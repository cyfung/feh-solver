package me.kumatheta.feh.skill.weapon

import me.kumatheta.feh.BasicWeapon
import me.kumatheta.feh.BattleState
import me.kumatheta.feh.InCombatSkill
import me.kumatheta.feh.InCombatStat
import me.kumatheta.feh.MagicR
import me.kumatheta.feh.Stat
import me.kumatheta.feh.WeaponType

class BladeWeapon(weaponType: WeaponType, might: Int) : BasicWeapon(weaponType, might) {
    override val coolDownCountAdj: Int
        get() = 1

    override val additionalInCombatStat: InCombatSkill<Stat>? = object : InCombatSkill<Stat> {
        override fun apply(battleState: BattleState, self: InCombatStat, foe: InCombatStat, initAttack: Boolean): Stat {
            return Stat(atk = self.bonus.totalExceptHp)
        }
    }
}

fun WeaponType.blade(might: Int): BladeWeapon {
    return BladeWeapon(this, might)
}