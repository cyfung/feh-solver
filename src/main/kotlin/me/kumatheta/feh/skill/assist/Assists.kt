package me.kumatheta.feh.skill.assist

import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.skill.toSkillMap

val BASE_ASSIST_COMPARATOR = me.kumatheta.feh.util.compareByDescending<HeroUnit>({
    it.currentStatTotal
}, {
    it.id
})

val ALL_ASSISTS = sequenceOf(
    "Reciprocal Aid" to ReciprocalAid,
    "Physics+" to HealingPlus(8, true, 0, 8),
    "Dance" to Refresh(),
    "Sing" to Refresh(),
    "Play" to Refresh()
).toSkillMap()