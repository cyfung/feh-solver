package me.kumatheta.feh.skill

import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.Stat
import me.kumatheta.feh.skill.assist.DrawBack
import me.kumatheta.feh.skill.assist.HealingPlus
import me.kumatheta.feh.skill.assist.Rally
import me.kumatheta.feh.skill.assist.ReciprocalAid
import me.kumatheta.feh.skill.assist.Refresh
import me.kumatheta.feh.skill.assist.Reposition
import me.kumatheta.feh.skill.assist.ToChangeFate
import me.kumatheta.feh.skill.passive.GentleDream

val BASE_ASSIST_COMPARATOR = me.kumatheta.feh.util.compareByDescending<HeroUnit>({
    it.currentStatTotal
}, {
    it.id
})

val ALL_ASSISTS = sequenceOf(
    "Reciprocal Aid" to ReciprocalAid,
    "Physic+" to HealingPlus(8, true, 0, 8),
    "Dance" to Refresh(),
    "Sing" to Refresh(),
    "Play" to Refresh(),

    "Gentle Dream" to GentleDream,
    "Rally Atk/Spd" to Rally(Stat(atk = 3, spd = 3)),
    "Reposition" to Reposition,
    "Draw Back" to DrawBack,
    "To Change Fate!" to ToChangeFate
).toSkillMap()