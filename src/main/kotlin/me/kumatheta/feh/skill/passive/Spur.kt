package me.kumatheta.feh.skill.passive

import me.kumatheta.feh.BattleState
import me.kumatheta.feh.CombatStartSkill
import me.kumatheta.feh.CombatSupportSkillMethod
import me.kumatheta.feh.ConstantCombatStartSkill
import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.Passive
import me.kumatheta.feh.Skill
import me.kumatheta.feh.Stat

object SpurDefRes2 : Passive {
    override val supportInCombatBuff: CombatSupportSkillMethod<Skill?>? = object : CombatSupportSkillMethod<Skill?> {
        override fun apply(battleState: BattleState, self: HeroUnit, target: HeroUnit): Skill? {
            return if (target.position.distanceTo(self.position) == 1) {
                object : Skill {
                    override val inCombatStat: CombatStartSkill<Stat>? =
                        ConstantCombatStartSkill(Stat(def = 3, res = 3))
                }
            } else {
                null
            }
        }
    }
}