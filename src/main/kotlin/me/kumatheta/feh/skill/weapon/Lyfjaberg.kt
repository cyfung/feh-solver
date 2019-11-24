package me.kumatheta.feh.skill.weapon

import me.kumatheta.feh.BasicWeapon
import me.kumatheta.feh.BattleState
import me.kumatheta.feh.CombatEndSkill
import me.kumatheta.feh.CombatStartSkill
import me.kumatheta.feh.ConstantInCombatSkill
import me.kumatheta.feh.Dagger
import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.InCombatSkill
import me.kumatheta.feh.Skill
import me.kumatheta.feh.Stat

object Lyfjaberg : BasicWeapon(Dagger, 14) {
    override val extraStat: Stat = Stat(res = 3)
    override val inCombatStat: CombatStartSkill<Stat>? = object : CombatStartSkill<Stat> {
        override fun apply(battleState: BattleState, self: HeroUnit, foe: HeroUnit, initAttack: Boolean): Stat {
            return if (self.hpThreshold(50) >= 0) {
                self.combatSkillData[this@Lyfjaberg] = true
                Stat(atk = 4, spd = 4)
            } else {
                Stat.ZERO
            }
        }
    }

    override val foeEffect: CombatStartSkill<Skill?>? = object : CombatStartSkill<Skill?> {
        override fun apply(battleState: BattleState, self: HeroUnit, foe: HeroUnit, initAttack: Boolean): Skill? {
            return if (initAttack && self.hpThreshold(50) >= 0) {
                return object : Skill {
                    override val followUp: InCombatSkill<Int>? = ConstantInCombatSkill(-1)
                }
            } else {
                null
            }
        }
    }

    override val combatEnd: CombatEndSkill? = object : CombatEndSkill {
        override fun apply(
            battleState: BattleState,
            self: HeroUnit,
            foe: HeroUnit,
            attack: Boolean,
            attacked: Boolean
        ) {
            if (attacked) {
                battleState.unitsSeq(foe.team).filter { it.position.distanceTo(foe.position) <= 2 }.forEach {
                    it.applyDebuff(Stat(def = -7, res = -7))
                }
                if (self.combatSkillData[this@Lyfjaberg] == true) {
                    self.endOfCombatEffects.takeNonLethalDamage(6)
                }
            }
        }
    }
}