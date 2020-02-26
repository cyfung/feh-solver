package me.kumatheta.feh

import kotlin.math.max
import kotlin.math.min

data class Stat(
    val hp: Int = 0,
    val atk: Int = 0,
    val spd: Int = 0,
    val def: Int = 0,
    val res: Int = 0
) {
    val totalExceptHp: Int
        get() = atk + spd + def + res

    companion object {
        val ZERO = Stat()
        val ONES = Stat(1, 1, 1, 1, 1)
    }

    operator fun plus(o: Stat): Stat {
        return when {
            this === ZERO -> o
            o === ZERO -> this
            else -> Stat(hp + o.hp, atk + o.atk, spd + o.spd, def + o.def, res + o.res)
        }
    }

    fun isZero(): Boolean {
        return this === ZERO || (hp == 0 && atk == 0 && spd == 0 && def == 0 && res == 0)
    }

    fun isNotZero(): Boolean {
        return !isZero()
    }

    operator fun unaryMinus(): Stat {
        return Stat(
            hp = -hp,
            atk = -atk,
            spd = -spd,
            def = -def,
            res = -res
        )
    }

    operator fun times(stat: Stat): Stat {
        if (this == ONES) {
            return stat
        }
        return Stat(
            hp = hp * stat.hp,
            atk = atk * stat.atk,
            spd = spd * stat.spd,
            def = def * stat.def,
            res = res * stat.res
        )
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