package me.kumatheta.feh.skill.effect

import me.kumatheta.feh.BattleState
import me.kumatheta.feh.HeroUnit

interface PassEffect: SkillEffect {
    fun canPass(battleState: BattleState, self: HeroUnit): Boolean
}

class Pass(private val percentageHp: Int): PassEffect {
    override fun canPass(battleState: BattleState, self: HeroUnit): Boolean {
        return self.hpThreshold(percentageHp) >= 0
    }
}
