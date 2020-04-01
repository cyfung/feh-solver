package me.kumatheta.feh.skill.passive

import me.kumatheta.feh.skill.assist.movement.ShoveEffect
import me.kumatheta.feh.skill.effect.PostInitiateMovement
import me.kumatheta.feh.skill.effect.others.RepelEffect
import me.kumatheta.feh.skill.effect.skillEffects
import me.kumatheta.feh.skill.toSkill

val Repel3 = skillEffects(RepelEffect, PostInitiateMovement(ShoveEffect)).toSkill()