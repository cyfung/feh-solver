package me.kumatheta.feh.skill.assist

import me.kumatheta.feh.HeroUnit

val BASE_ASSIST_COMPARATOR = me.kumatheta.feh.util.compareByDescending<HeroUnit>({
    it.currentStatTotal
}, {
    it.id
})