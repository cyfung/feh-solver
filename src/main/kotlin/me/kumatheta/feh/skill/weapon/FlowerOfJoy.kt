package me.kumatheta.feh.skill.weapon

import me.kumatheta.feh.BasicWeapon
import me.kumatheta.feh.BattleState
import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.MagicB
import me.kumatheta.feh.MapSkillWithTarget
import me.kumatheta.feh.Skill
import me.kumatheta.feh.Stat
import me.kumatheta.feh.combatStartSkill
import me.kumatheta.feh.skill.inCardinalDirection
import me.kumatheta.feh.skill.passive.toInCombatStatPassive

private val IN_COMBAT_BUFF = Stat(atk = 3, spd = 3)

object FlowerOfJoy : BasicWeapon(MagicB, 14) {
    override val extraStat: Stat = Stat(res = 3)

    override val supportInCombatBuff: MapSkillWithTarget<Skill?>? = object : MapSkillWithTarget<Skill?> {
        override fun apply(battleState: BattleState, self: HeroUnit, target: HeroUnit): Skill? {
            return if (self.inCardinalDirection(target)) {
                combatStartSkill(IN_COMBAT_BUFF).toInCombatStatPassive()
            } else {
                null
            }
        }
    }
}