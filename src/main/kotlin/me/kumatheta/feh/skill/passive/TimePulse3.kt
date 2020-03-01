package me.kumatheta.feh.skill.passive

import me.kumatheta.feh.BattleState
import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.skill.MapSkillMethod

val TimePulse3: MapSkillMethod<Unit> = { _: BattleState, self: HeroUnit ->
    if (self.cooldown == self.cooldownCount) {
        self.cachedEffect.cooldown--
    }
}
