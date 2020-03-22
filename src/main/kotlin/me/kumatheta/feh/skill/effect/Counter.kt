package me.kumatheta.feh.skill.effect

import me.kumatheta.feh.StatType

interface Counter : SkillEffect, CombatStartEffect<Sequence<StatType>>