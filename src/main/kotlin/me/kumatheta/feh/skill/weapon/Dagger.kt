package me.kumatheta.feh.skill.weapon

import me.kumatheta.feh.DaggerC
import me.kumatheta.feh.Stat
import me.kumatheta.feh.allDragonType
import me.kumatheta.feh.skill.CombatStatus
import me.kumatheta.feh.skill.InCombatStat
import me.kumatheta.feh.skill.basic
import me.kumatheta.feh.skill.effect.EffectiveAgainstWeapon
import me.kumatheta.feh.skill.effect.ExtraInCombatStatEffect
import me.kumatheta.feh.skill.effect.postcombat.Dagger7Eff
import me.kumatheta.feh.skill.effect.skillEffects
import me.kumatheta.feh.skill.plus

val TheCleanerPlus = DaggerC.basic(12) + skillEffects(Dagger7Eff,
    object : ExtraInCombatStatEffect {
        override fun apply(combatStatus: CombatStatus<InCombatStat>): Stat {
            return Stat(atk = combatStatus.foe.bonus.totalExceptHp)
        }
    })

val DragonSlasherPlus = DaggerC.basic(12) + Dagger7Eff + allDragonType.asSequence().map {
    EffectiveAgainstWeapon(it)
}
