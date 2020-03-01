package me.kumatheta.feh.skill.passive

import me.kumatheta.feh.skill.BasicSkill
import me.kumatheta.feh.skill.MapSkillMethod
import me.kumatheta.feh.skill.Passive

class Pass(percentageHp: Int) : BasicSkill() {
    override val pass: MapSkillMethod<Boolean>? = { _, self ->
        self.hpThreshold(percentageHp) >= 0
    }
}