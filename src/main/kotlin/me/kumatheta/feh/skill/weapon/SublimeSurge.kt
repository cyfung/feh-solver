package me.kumatheta.feh.skill.weapon

import me.kumatheta.feh.DragonR
import me.kumatheta.feh.skill.basic
import me.kumatheta.feh.skill.effect.CounterAnyRangeBasic
import me.kumatheta.feh.skill.effect.NeutralizeEffectiveAgainstWeapon
import me.kumatheta.feh.skill.effect.others.DragonAdaptive
import me.kumatheta.feh.skill.effect.skillEffects
import me.kumatheta.feh.skill.plus

val SublimeSurge = DragonR.basic(16) + skillEffects(
    DragonAdaptive,
    CounterAnyRangeBasic,
    NeutralizeEffectiveAgainstWeapon(DragonR)
)