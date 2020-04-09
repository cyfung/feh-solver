package me.kumatheta.feh.skill

import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.Stat
import me.kumatheta.feh.skill.assist.GentleDream
import me.kumatheta.feh.skill.assist.HealingPlus
import me.kumatheta.feh.skill.assist.Rally
import me.kumatheta.feh.skill.assist.RallyUp
import me.kumatheta.feh.skill.assist.ReciprocalAid
import me.kumatheta.feh.skill.assist.Recover
import me.kumatheta.feh.skill.assist.RecoverPlus
import me.kumatheta.feh.skill.assist.Refresh
import me.kumatheta.feh.skill.assist.RestorePlus
import me.kumatheta.feh.skill.assist.Sacrifice
import me.kumatheta.feh.skill.assist.movement.DrawBack
import me.kumatheta.feh.skill.assist.movement.Pivot
import me.kumatheta.feh.skill.assist.movement.Reposition
import me.kumatheta.feh.skill.assist.movement.Shove
import me.kumatheta.feh.skill.assist.movement.Smite
import me.kumatheta.feh.skill.assist.movement.Swap
import me.kumatheta.feh.skill.assist.movement.ToChangeFate
import me.kumatheta.feh.statPairSequence
import me.kumatheta.feh.toStat

val BASE_ASSIST_COMPARATOR = me.kumatheta.feh.util.compareByDescending<HeroUnit>({
    it.currentStatTotal
}, {
    it.id
})

val ALL_ASSISTS = (
        statPairSequence {
            "Rally ${it.first}/${it.second}+" to Rally(it.toStat(6))
        } + sequenceOf(
            "Reciprocal Aid" to ReciprocalAid,
            "Physic+" to HealingPlus(8, true, 0, 8),
            "Restore+" to RestorePlus,
            "Recover+" to RecoverPlus,
            "Recover" to Recover,
            "Dance" to Refresh(),
            "Sing" to Refresh(),
            "Play" to Refresh(),
            "Rally Up Atk+" to RallyUp(Stat(atk = 6)),
            "Sacrifice" to Sacrifice,

            "Gentle Dream" to GentleDream,
            "Rally Atk/Spd" to Rally(Stat(atk = 3, spd = 3)),
            "Reposition" to Reposition,
            "Draw Back" to DrawBack,
            "Shove" to Shove,
            "Smite" to Smite,
            "Pivot" to Pivot,
            "Swap" to Swap,
            "To Change Fate!" to ToChangeFate
        )
        ).toSkillMap()
