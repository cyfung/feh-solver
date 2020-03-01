package me.kumatheta.feh.skill.passive

import me.kumatheta.feh.skill.BasicSkill

val LiveToServe3 = BasicSkill(
    onHealOthers = { _, self, _, healAmount ->
        self.heal(healAmount)
    }
)