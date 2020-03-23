package me.kumatheta.feh.skill.passive

import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.Stat
import me.kumatheta.feh.foe
import me.kumatheta.feh.skill.CombatStatus
import me.kumatheta.feh.skill.InCombatStat
import me.kumatheta.feh.skill.effect.PostCombatEffect

class Smoke(private val effect: (HeroUnit) -> Unit) : PostCombatEffect {
    override fun onCombatEnd(combatStatus: CombatStatus<InCombatStat>, attacked: Boolean) {
        if (attacked && !combatStatus.self.heroUnit.isDead) {
            combatStatus.battleState.unitsSeq(combatStatus.self.heroUnit.team.foe).filter {
                it.position.distanceTo(combatStatus.self.heroUnit.position) <= 2
            }.forEach {
                effect(it)
            }
        }
    }
}

fun smoke(debuff: Stat): Smoke {
    return Smoke {
        it.applyDebuff(debuff)
    }
}
fun smoke(atk: Int = 0,
           spd: Int = 0,
           def: Int = 0,
           res: Int = 0
) = smoke(Stat(atk = atk, spd = spd, def = def, res = res))


val pulseSmoke3 = Smoke {
    it.cachedEffect.cooldown++
}
