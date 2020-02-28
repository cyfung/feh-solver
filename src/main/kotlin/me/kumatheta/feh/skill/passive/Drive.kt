package me.kumatheta.feh.skill.passive

import me.kumatheta.feh.BattleState
import me.kumatheta.feh.CombatStartSkill
import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.MapSkillWithTarget
import me.kumatheta.feh.MoveType
import me.kumatheta.feh.Passive
import me.kumatheta.feh.Skill
import me.kumatheta.feh.Stat
import me.kumatheta.feh.combatSkill
import me.kumatheta.feh.combatStartSkill
import me.kumatheta.feh.skill.toInCombatStatPassive

class Drive(buff: Stat) : Passive {
    private val skill = combatStartSkill(buff).toInCombatStatPassive()

    override val supportInCombatBuff: MapSkillWithTarget<Skill?>? = object : MapSkillWithTarget<Skill?> {
        override fun apply(battleState: BattleState, self: HeroUnit, target: HeroUnit): Skill? {
            return if (target.position.distanceTo(self.position) <= 2) {
                skill
            } else {
                null
            }
        }
    }
}

object GoadCavalry : Passive {
    override val supportInCombatBuff: MapSkillWithTarget<Skill?>? = object : MapSkillWithTarget<Skill?> {
        override fun apply(battleState: BattleState, self: HeroUnit, target: HeroUnit): Skill? {
            return if (target.moveType == MoveType.CAVALRY && target.position.distanceTo(self.position) <= 2) {
                object : Skill {
                    override val inCombatStat: CombatStartSkill<Stat>? = combatSkill(Stat(atk = 4, spd = 4))
                }
            } else {
                null
            }
        }
    }
}