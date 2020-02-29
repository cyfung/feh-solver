package me.kumatheta.feh.skill.passive

import me.kumatheta.feh.CombatStatus
import me.kumatheta.feh.InCombatStat
import me.kumatheta.feh.MapSkillMethod
import me.kumatheta.feh.Passive
import me.kumatheta.feh.skill.effect.wrathDamage
import me.kumatheta.feh.skill.effect.wrathSpecialCharge

class Wrath(hpPercentage: Int) : Passive {
    override val startOfTurn: MapSkillMethod<Unit>? = wrathSpecialCharge(hpPercentage)

    override val damageIncrease: ((CombatStatus<InCombatStat>, specialTriggered: Boolean) -> Int)? = wrathDamage(hpPercentage)
}