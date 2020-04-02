package me.kumatheta.feh.skill.weapon

import me.kumatheta.feh.MagicG
import me.kumatheta.feh.MoveType
import me.kumatheta.feh.Stat
import me.kumatheta.feh.skill.CombatStatus
import me.kumatheta.feh.skill.InCombatStat
import me.kumatheta.feh.skill.basic
import me.kumatheta.feh.skill.effect.EffectiveAgainstMovement
import me.kumatheta.feh.skill.effect.ExtraInCombatStatEffect
import me.kumatheta.feh.skill.effect.skillEffects
import me.kumatheta.feh.skill.plus

val LightOfDawn = MagicG.basic(14, res=3) + skillEffects(
    EffectiveAgainstMovement(MoveType.ARMORED),
    EffectiveAgainstMovement(MoveType.CAVALRY),
    object : ExtraInCombatStatEffect {
        override fun apply(combatStatus: CombatStatus<InCombatStat>): Stat {
            return combatStatus.foe.penalty
        }
    }
)