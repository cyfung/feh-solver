package me.kumatheta.feh.skill.passive

import me.kumatheta.feh.BattleState
import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.skill.BasicSkill
import me.kumatheta.feh.skill.MapSkillMethod
import me.kumatheta.feh.skill.Passive

object SparklingBoost : BasicSkill() {
    override val startOfTurn: MapSkillMethod<Unit>? = { battleState: BattleState, self: HeroUnit ->
        val allyWithHighestHpLost = battleState.unitsSeq(self.team).filterNot { it == self }.map {
            val hpLost = it.maxHp - it.currentHp
            it to hpLost
        }.groupBy({ it.second }, { it.first }).maxBy { it.key }?.value
        if (!allyWithHighestHpLost.isNullOrEmpty()) {
            allyWithHighestHpLost.forEach { it.heal(10) }
        }
    }
}
