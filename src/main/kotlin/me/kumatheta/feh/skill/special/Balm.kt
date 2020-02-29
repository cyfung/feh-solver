package me.kumatheta.feh.skill.special

import me.kumatheta.feh.BattleState
import me.kumatheta.feh.HealingSpecial
import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.Stat
import me.kumatheta.feh.skill.allies

class Balm(private val buff: Stat) : HealingSpecial(1) {
    override fun trigger(self: HeroUnit, target: HeroUnit, battleState: BattleState) {
        self.allies(battleState).forEach {
            it.applyBuff(buff)
        }
    }
}