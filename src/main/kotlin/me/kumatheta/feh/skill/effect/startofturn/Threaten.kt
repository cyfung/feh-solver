package me.kumatheta.feh.skill.effect.startofturn

import me.kumatheta.feh.BattleState
import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.Stat
import me.kumatheta.feh.foe
import me.kumatheta.feh.skill.effect.StartOfTurnEffect
import me.kumatheta.feh.skill.effect.postcombat.Seal
import me.kumatheta.feh.statPairSequence
import me.kumatheta.feh.statSequence
import me.kumatheta.feh.toStat

class Threaten(private val stat: Stat): StartOfTurnEffect {
    override fun onStartOfTurn(battleState: BattleState, self: HeroUnit) {
        val position = self.position
        battleState.unitsSeq(self.team.foe).filter { it.position.distanceTo(position) <= 2 }.forEach {
            it.cachedEffect.applyDebuff(stat)
        }
    }
}

fun threaten(atk: Int = 0,
             spd: Int = 0,
             def: Int = 0,
             res: Int = 0
) = Threaten(Stat(atk = atk, spd = spd, def = def, res = res))

fun allThreaten() = (1..2).asSequence().flatMap { level ->
    statPairSequence {
        val name = "Threat. ${it.first}/${it.second} $level"
        name to Threaten(it.toStat(-(2 + level)))
    }
} + (1..3).asSequence().flatMap { level ->
    statSequence {
        val name = "Threaten $it $level"
        name to Threaten(it.toStat(-(2 + level)))
    }
}