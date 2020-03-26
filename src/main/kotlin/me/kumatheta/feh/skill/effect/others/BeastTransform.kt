package me.kumatheta.feh.skill.effect.others

import me.kumatheta.feh.BattleState
import me.kumatheta.feh.Beast
import me.kumatheta.feh.Dragon
import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.PositiveStatus
import me.kumatheta.feh.Stat
import me.kumatheta.feh.skill.adjacentAllies
import me.kumatheta.feh.skill.effect.Transform

private fun baseTransform(battleState: BattleState, self: HeroUnit): Boolean {
    return if (self.adjacentAllies(battleState).map { it.weaponType }.all {
            it is Dragon || it is Beast
        }) {
        self.addPositiveStatus(PositiveStatus.TRANSFORMED)
        true
    } else {
        false
    }
}

object FlyingBeast : Transform {
    private val EXTRA_STAT = Stat(atk = 2)

    override fun transform(battleState: BattleState, self: HeroUnit): Stat {
        return if (baseTransform(battleState, self)) {
            self.addPositiveStatus(PositiveStatus.EXTRA_TRAVEL_POWER)
            EXTRA_STAT
        } else {
            Stat.ZERO
        }
    }

}