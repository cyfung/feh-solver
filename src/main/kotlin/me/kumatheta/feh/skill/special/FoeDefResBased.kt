package me.kumatheta.feh.skill.special

import me.kumatheta.feh.BattleState
import me.kumatheta.feh.skill.DamagingSpecial
import me.kumatheta.feh.skill.InCombatStat
import me.kumatheta.feh.skill.effect.SkillEffect

open class FoeDefResBased(
    coolDownCount: Int,
    private val multiplier: Int,
    private val divider: Int,
    vararg skillEffects: SkillEffect
) : DamagingSpecial(coolDownCount, *skillEffects) {
    override fun getDamage(
        battleState: BattleState,
        self: InCombatStat,
        foe: InCombatStat,
        defenderDefRes: Int,
        atk: Int
    ): Int {
        return defenderDefRes * multiplier / divider
    }
}