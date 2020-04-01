package me.kumatheta.feh.skill.effect.incombatstat

import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.Stat
import me.kumatheta.feh.StatType
import me.kumatheta.feh.skill.CombatStatus
import me.kumatheta.feh.skill.effect.InCombatStatEffect
import me.kumatheta.feh.skill.nearbyAllies
import me.kumatheta.feh.statPairSequence
import me.kumatheta.feh.toStat

class Form3(private val statPair: Pair<StatType, StatType>) : InCombatStatEffect {
    override fun apply(combatStatus: CombatStatus<HeroUnit>): Stat {
        val (battleState, self) = combatStatus
        val nearbyAlliesCount = self.nearbyAllies(battleState, 2).count()
        return if (nearbyAlliesCount > 0) {
            statPair.toStat(2 * nearbyAlliesCount + 1)
        } else {
            Stat.ZERO
        }
    }
}

fun allForm() = statPairSequence {
    val name = "${it.first}/${it.second} Form 3"
    name to Form3(it)
}