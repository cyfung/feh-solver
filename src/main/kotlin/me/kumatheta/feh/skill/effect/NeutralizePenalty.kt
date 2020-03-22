package me.kumatheta.feh.skill.effect

import me.kumatheta.feh.StatType

interface NeutralizePenalty : SkillEffect, CombatStartEffect<Sequence<StatType>>