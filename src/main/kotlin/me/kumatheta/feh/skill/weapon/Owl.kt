package me.kumatheta.feh.skill.weapon

import me.kumatheta.feh.BasicWeapon
import me.kumatheta.feh.CombatStartSkill
import me.kumatheta.feh.Stat
import me.kumatheta.feh.WeaponType
import me.kumatheta.feh.skill.adjacentAllies

class Owl(weaponType: WeaponType, might: Int) : BasicWeapon(weaponType, might) {
    override val inCombatStat: CombatStartSkill<Stat>? = { combatStatus ->
        val (battleState, self) = combatStatus
        val buff = self.adjacentAllies(battleState).count() * 2
        Stat(atk = buff, spd = buff, def = buff, res = buff)
    }
}


fun WeaponType.owl(might: Int): Owl {
    return Owl(this, might)
}