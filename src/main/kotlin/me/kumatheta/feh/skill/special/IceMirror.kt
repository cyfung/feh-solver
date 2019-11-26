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

    override val damageReceivedListener: PerAttackListener<Int>? = object : PerAttackListener<Int> {
        override fun onReceive(battleState: BattleState, self: InCombatStat, foe: InCombatStat, value: Int) {
            val state = self.heroUnit.combatSkillData[IceMirror] as State? ?: return
            if (state.damageReduced == null) {
                self.heroUnit.combatSkillData[IceMirror] = State(value)
            }
        }
    }

    override val damageIncrease: PerAttackSkill<Int>? = object : PerAttackSkill<Int> {
        override fun apply(
            battleState: BattleState,
            self: InCombatStat,
            foe: InCombatStat,
            specialTriggered: Boolean
        ): Int {
            val state = self.heroUnit.combatSkillData[IceMirror] as State? ?: return 0
            val damageReduced = state.damageReduced ?: throw IllegalStateException()
            self.heroUnit.combatSkillData.remove(IceMirror)
            return damageReduced
        }
    }

    class State(val damageReduced: Int?)

}