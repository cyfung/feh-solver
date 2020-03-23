package me.kumatheta.feh.skill.weapon

import me.kumatheta.feh.Axe
import me.kumatheta.feh.skill.basic
import me.kumatheta.feh.skill.effect.TriangleAdept
import me.kumatheta.feh.skill.effect.startofturn.tactics
import me.kumatheta.feh.skill.plus

val DraconicPoleaxEff = Axe.basic(16, hp = 3) + TriangleAdept(20) + tactics(res = 6)