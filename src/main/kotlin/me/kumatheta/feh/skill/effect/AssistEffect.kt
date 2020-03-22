package me.kumatheta.feh.skill.effect

import me.kumatheta.feh.BattleState
import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.skill.Assist

interface AssistEffect : SkillEffect {
    fun onAssist(battleState: BattleState, self: HeroUnit, ally: HeroUnit, assist: Assist, selfUseAssist: Boolean)
}