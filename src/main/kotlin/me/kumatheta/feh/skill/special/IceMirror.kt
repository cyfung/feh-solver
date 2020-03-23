package me.kumatheta.feh.skill.special

import me.kumatheta.feh.BattleState
import me.kumatheta.feh.skill.CombatStatus
import me.kumatheta.feh.skill.DamageDealt
import me.kumatheta.feh.skill.DefenseSpecial
import me.kumatheta.feh.skill.InCombatStat
import me.kumatheta.feh.skill.effect.DamageIncrease
import me.kumatheta.feh.skill.effect.DamageReceivedListener

private const val ID = "IceMirror"

object IceMirror : DefenseSpecial(2,
    object : DamageReceivedListener {
        override fun onDamageReceived(combatStatus: CombatStatus<InCombatStat>, damageReceived: DamageDealt) {
            val state = combatStatus.self.heroUnit.combatSkillData[ID] as State? ?: return
            if (state.damageReduced == null) {
                combatStatus.self.heroUnit.combatSkillData[ID] = State(damageReceived.damageReduced)
            }
        }
    },
    object : DamageIncrease {
        override fun getDamageIncrease(combatStatus: CombatStatus<InCombatStat>, specialTriggered: Boolean): Int {
            val state = combatStatus.self.heroUnit.combatSkillData[ID] as State? ?: return 0
            val damageReduced = state.damageReduced ?: throw IllegalStateException()
            combatStatus.self.heroUnit.combatSkillData.remove(ID)
            return damageReduced
        }
    }
) {
    override fun getReducedDamage(
        battleState: BattleState,
        self: InCombatStat,
        foe: InCombatStat,
        incomingDamage: Int
    ): Int? {
        return if (foe.heroUnit.weaponType.isRanged) {
            self.heroUnit.combatSkillData[ID] = State(null)
            incomingDamage - incomingDamage * 3 / 10
        } else {
            null
        }
    }

    class State(val damageReduced: Int?)
}


