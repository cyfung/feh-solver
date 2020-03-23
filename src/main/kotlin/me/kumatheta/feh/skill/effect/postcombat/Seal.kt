package me.kumatheta.feh.skill.effect.postcombat

import me.kumatheta.feh.Stat
import me.kumatheta.feh.skill.CombatStatus
import me.kumatheta.feh.skill.InCombatStat
import me.kumatheta.feh.skill.effect.PostCombatEffect

class Seal(private val stat: Stat) : PostCombatEffect {
    override fun onCombatEnd(combatStatus: CombatStatus<InCombatStat>, attacked: Boolean) {
        if (!combatStatus.self.heroUnit.isDead && !combatStatus.foe.heroUnit.isDead) {
            combatStatus.foe.heroUnit.applyDebuff(stat)
        }
    }
}

fun seal(
    atk: Int = 0,
    spd: Int = 0,
    def: Int = 0,
    res: Int = 0
) = Seal(Stat(atk = atk, spd = spd, def = def, res = res))