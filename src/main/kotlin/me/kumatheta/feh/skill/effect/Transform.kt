package me.kumatheta.feh.skill.effect

import me.kumatheta.feh.BattleState
import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.Stat

interface Transform : SkillEffect {
    fun transform(battleState: BattleState, self: HeroUnit) : Stat
}