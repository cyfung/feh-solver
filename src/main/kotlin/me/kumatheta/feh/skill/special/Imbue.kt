package me.kumatheta.feh.skill.special

import me.kumatheta.feh.BattleState
import me.kumatheta.feh.skill.HealingSpecial
import me.kumatheta.feh.HeroUnit

object Imbue : HealingSpecial(1) {
    override fun trigger(self: HeroUnit, target: HeroUnit, battleState: BattleState) {
        // the heal bonus is applied by the heal skill already
    }

    const val healBonus = 10
}