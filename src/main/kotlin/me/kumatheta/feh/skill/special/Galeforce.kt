package me.kumatheta.feh.skill.special

import me.kumatheta.feh.BattleState
import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.skill.PostCombatSpecial

object Galeforce: PostCombatSpecial(5) {
    override fun postCombat(battleState: BattleState, self: HeroUnit) {
        //FIXME no repeat on same turn
        self.refresh()
    }
}