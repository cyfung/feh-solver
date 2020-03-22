package me.kumatheta.feh.skill.effect

import me.kumatheta.feh.StatType

interface NeutralizeBonus : SkillEffect, CombatStartEffect<Sequence<StatType>>