package me.kumatheta.feh.skill.weapon

import me.kumatheta.feh.BasicWeapon
import me.kumatheta.feh.BattleState
import me.kumatheta.feh.Color
import me.kumatheta.feh.InCombatSkill
import me.kumatheta.feh.InCombatStat
import me.kumatheta.feh.Magic
import me.kumatheta.feh.Stat

object RaudrbladePlus : BasicWeapon(Magic(Color.RED), 14) {
    override val coolDownCountAdj: Int
        get() = 1

    override val additionalInCombatStat: InCombatSkill<Stat>? = object : InCombatSkill<Stat> {
        override fun apply(battleState: BattleState, self: InCombatStat, foe: InCombatStat, initAttack: Boolean): Stat {
            return Stat(atk = self.inCombatStat.totalExceptHp)
        }
    }
}