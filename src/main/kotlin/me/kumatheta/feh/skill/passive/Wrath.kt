package me.kumatheta.feh.skill.passive

import me.kumatheta.feh.MapSkillMethod
import me.kumatheta.feh.Passive
import me.kumatheta.feh.PerAttackSkill
import me.kumatheta.feh.skill.effect.wrathDamage
import me.kumatheta.feh.skill.effect.wrathSpecialCharge

class Wrath(hpPercentage: Int) : Passive {
    override val startOfTurn: MapSkillMethod<Unit>? = wrathSpecialCharge(hpPercentage)

    override val damageIncrease: PerAttackSkill<Int>? = wrathDamage(hpPercentage)
}