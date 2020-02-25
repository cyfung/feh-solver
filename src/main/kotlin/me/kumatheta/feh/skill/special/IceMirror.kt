package me.kumatheta.feh.skill.special

import me.kumatheta.feh.BattleState
import me.kumatheta.feh.DefenseSpecial
import me.kumatheta.feh.InCombatStat
import me.kumatheta.feh.PerAttackListener
import me.kumatheta.feh.PerAttackSkill

object IceMirror : DefenseSpecial(2) {
    override fun getReducedDamage(
        battleState: BattleState,
        self: InCombatStat,
        foe: InCombatStat,
        incomingDamage: Int
    ): Int? {
        return if (foe.heroUnit.weaponType.isRanged) {
            self.heroUnit.combatSkillData[this] = State(null)
            incomingDamage - incomingDamage * 3 / 10
        } else {
            null
        }
    }

    override val damageReducedListener: PerAttackListener<Int>? = lambda@ { combatStatus, value ->
        val state = combatStatus.self.heroUnit.combatSkillData[IceMirror] as State? ?: return@lambda
        if (state.damageReduced == null) {
            combatStatus.self.heroUnit.combatSkillData[IceMirror] = State(value)
        }
    }


    override val damageIncrease: PerAttackSkill<Int>? = lambda@ { combatStatus, _ ->
        val state = combatStatus.self.heroUnit.combatSkillData[IceMirror] as State? ?: return@lambda 0
        val damageReduced = state.damageReduced ?: throw IllegalStateException()
        combatStatus.self.heroUnit.combatSkillData.remove(IceMirror)
        damageReduced
    }

    class State(val damageReduced: Int?)

}


