package me.kumatheta.feh.skill.passive

import me.kumatheta.feh.skill.*
import me.kumatheta.feh.skill.effect.wrathDamage
import me.kumatheta.feh.skill.effect.wrathSpecialCharge

class Wrath(hpPercentage: Int) : BasicSkill() {
    override val startOfTurn: MapSkillMethod<Unit>? = wrathSpecialCharge(hpPercentage)

    override val damageIncrease: ((CombatStatus<InCombatStat>, specialTriggered: Boolean) -> Int)? = wrathDamage(hpPercentage)
}