package me.kumatheta.feh.skill.effect

import me.kumatheta.feh.BattleState
import me.kumatheta.feh.HeroUnit

interface PassEffect {
    fun canPass(battleState: BattleState, self: HeroUnit): Boolean
}