package me.kumatheta.feh.skill.effect

import me.kumatheta.feh.BattleState
import me.kumatheta.feh.HeroUnit

interface ObstructEffect: SkillEffect {
    fun canObstruct(battleState: BattleState, self: HeroUnit): Boolean
}

class Obstruct(private val percentageHp: Int): ObstructEffect {
    override fun canObstruct(battleState: BattleState, self: HeroUnit): Boolean {
        return self.hpThreshold(percentageHp) >= 0
    }
}
