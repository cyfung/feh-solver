package com.bloombase.feh

import kotlin.math.max
import kotlin.math.min

class Stat(
    val hp: Int = 0,
    val atk: Int = 0,
    val spd: Int = 0,
    val def: Int = 0,
    val res: Int = 0
) {
    companion object {
        val ZERO = Stat()
    }

    operator fun plus(o: Stat): Stat {
        return when {
            this == ZERO -> o
            o == ZERO -> this
            else -> Stat(hp + o.hp, atk + o.atk, spd + o.spd, def + o.def, res + o.res)
        }
    }

}

fun min(s1: Stat, s2: Stat): Stat {
    return Stat(
        hp = min(s1.hp, s2.hp),
        atk = min(s1.atk, s2.atk),
        spd = min(s1.spd, s2.spd),
        def = min(s1.def, s2.def),
        res = min(s1.res, s2.res)
    )
}

fun max(s1: Stat, s2: Stat): Stat {
    return Stat(
        hp = max(s1.hp, s2.hp),
        atk = max(s1.atk, s2.atk),
        spd = max(s1.spd, s2.spd),
        def = max(s1.def, s2.def),
        res = max(s1.res, s2.res)
    )
}