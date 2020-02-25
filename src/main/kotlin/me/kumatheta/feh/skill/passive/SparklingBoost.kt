package me.kumatheta.feh.skill.passive

import me.kumatheta.feh.BattleState
import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.MapSkillMethod
import me.kumatheta.feh.Passive

object SparklingBoost : Passive {
    override val startOfTurn: MapSkillMethod<Unit>? = { battleState: BattleState, self: HeroUnit ->
        val allyWithHighestHpLost = battleState.unitsSeq(self.team).filterNot { it == self }.map {
            val hpLost = it.stat.hp - it.currentHp
            it to hpLost
        }.groupBy({ it.second }, { it.first }).maxBy { it.key }?.value
        if (!allyWithHighestHpLost.isNullOrEmpty()) {
            allyWithHighestHpLost.forEach { it.heal(10) }
        }
    }
}
