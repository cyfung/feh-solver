package me.kumatheta.feh.skill.effect

import me.kumatheta.feh.Stat

interface InCombatStatEffect : SkillEffect, CombatStartEffect<Stat>

