package me.kumatheta.feh.skill.weapon

import me.kumatheta.feh.Staff
import me.kumatheta.feh.skill.basic
import me.kumatheta.feh.skill.effect.SpecialDebuff
import me.kumatheta.feh.skill.effect.postcombat.panicEffect
import me.kumatheta.feh.skill.effect.postcombat.trilemmaEffect
import me.kumatheta.feh.skill.plus

val TrilemmaPlus = Staff.basic(12) + trilemmaEffect + SpecialDebuff.ALWAYS_AVAILABLE

val PanicPlus = Staff.basic(11) + panicEffect + SpecialDebuff.ALWAYS_AVAILABLE

