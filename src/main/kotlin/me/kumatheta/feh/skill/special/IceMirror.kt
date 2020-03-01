package me.kumatheta.feh.skill.special

import me.kumatheta.feh.*
import me.kumatheta.feh.skill.*

private const val ID = "IceMirror"
object IceMirror : DefenseSpecial(2) {
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

    override val damageReceivedListener: PerAttackListener<DamageDealt>? = lambda@{ combatStatus, damageDealt ->
        val state = combatStatus.self.heroUnit.combatSkillData[ID] as State? ?: return@lambda
        if (state.damageReduced == null) {
            combatStatus.self.heroUnit.combatSkillData[ID] = State(damageDealt.damageReduced)
        }
    }


    override val damageIncrease: ((CombatStatus<InCombatStat>, specialTriggered: Boolean) -> Int)? = lambda@{ combatStatus, _ ->
        val state = combatStatus.self.heroUnit.combatSkillData[ID] as State? ?: return@lambda 0
        val damageReduced = state.damageReduced ?: throw IllegalStateException()
        combatStatus.self.heroUnit.combatSkillData.remove(ID)
        damageReduced
    }

    class State(val damageReduced: Int?)

}


