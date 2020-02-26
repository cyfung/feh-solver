package me.kumatheta.feh.skill

import me.kumatheta.feh.Skill

class SkillMap<T : Skill>(seq: Sequence<Pair<String, T>>) {
    private val map = seq.associate {
        it.first.normalize() to it.second
    }

    operator fun get(s: String): T {
        return map[s.normalize()] ?: throw IllegalArgumentException("skill not found $s")
    }
}

private fun String.normalize() =
    toLowerCase().replace("[^a-z0-9+]".toRegex(), "")

fun <T : Skill> Sequence<Pair<String, T>>.toSkillMap(): SkillMap<T> {
    return SkillMap(this)
}