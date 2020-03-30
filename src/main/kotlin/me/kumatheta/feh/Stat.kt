package me.kumatheta.feh

import kotlin.math.max
import kotlin.math.min

enum class StatType {
    ATK,
    SPD,
    DEF,
    RES
}

val ALL_STAT_TYPE_PAIRS = listOf(
    StatType.ATK to StatType.SPD,
    StatType.ATK to StatType.DEF,
    StatType.ATK to StatType.RES,
    StatType.SPD to StatType.DEF,
    StatType.SPD to StatType.RES,
    StatType.DEF to StatType.RES
)

fun Pair<StatType, StatType>.contains(statType: StatType): Boolean {
    return first == statType || second == statType
}

fun Pair<StatType, StatType>.toStat(value: Int): Stat {
    return Stat(
        atk = if (contains(StatType.ATK)) value else 0,
        spd = if (contains(StatType.SPD)) value else 0,
        def = if (contains(StatType.DEF)) value else 0,
        res = if (contains(StatType.RES)) value else 0
    )
}

fun <R> statPairSequence(transform: (Pair<StatType, StatType>) -> R) = ALL_STAT_TYPE_PAIRS.asSequence().map(transform)

fun <R> statSequence(transform: (StatType) -> R) = ALL_STAT_TYPES.asSequence().map(transform)

val ALL_STAT_TYPES = StatType.values().asList()

fun StatType.toStat(value: Int): Stat {
    return when(this) {
        StatType.ATK -> Stat(atk=value)
        StatType.SPD -> Stat(spd= value)
        StatType.DEF -> Stat(def= value)
        StatType.RES -> Stat(res= value)
    }
}


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