package me.kumatheta.feh.skill.passive

import me.kumatheta.feh.BattleState
import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.MapSkillWithTarget
import me.kumatheta.feh.Skill
import me.kumatheta.feh.Stat
import me.kumatheta.feh.combatStartSkill
import me.kumatheta.feh.skill.toInCombatStatPassive

class Spur(private val stat: Stat) : MapSkillWithTarget<Skill?> {
    override fun apply(battleState: BattleState, self: HeroUnit, target: HeroUnit): Skill? {
        return if (target.position.distanceTo(self.position) == 1) {
            combatStartSkill(stat).toInCombatStatPassive()
        } else {
            null
        }
    }
}