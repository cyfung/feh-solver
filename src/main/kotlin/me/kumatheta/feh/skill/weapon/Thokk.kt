package me.kumatheta.feh.skill.weapon

import me.kumatheta.feh.BattleState
import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.NegativeStatus
import me.kumatheta.feh.Staff
import me.kumatheta.feh.foe
import me.kumatheta.feh.skill.basic
import me.kumatheta.feh.skill.effect.StaffAsNormalBasic
import me.kumatheta.feh.skill.effect.StartOfTurnEffect
import me.kumatheta.feh.skill.effect.skillEffects
import me.kumatheta.feh.skill.plus

val Thokk = Staff.basic(14) + skillEffects(
    StaffAsNormalBasic,
    object : StartOfTurnEffect {
        override fun onStartOfTurn(battleState: BattleState, self: HeroUnit) {
            battleState.unitsSeq(self.team.foe).filter {
                it.currentHp <= self.currentHp - 3 && (it.position.x == self.position.x || it.position.y == self.position.y)
            }.filter {
                it.weaponType.isRanged
            }.forEach {
                it.addNegativeStatus(NegativeStatus.GRAVITY)
            }
        }
    }
)