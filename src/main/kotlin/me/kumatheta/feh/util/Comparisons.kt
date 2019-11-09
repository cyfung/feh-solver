package me.kumatheta.feh.util

public fun <T> compareByDescending(vararg selectors: (T) -> Comparable<*>?): Comparator<T> {
    require(selectors.isNotEmpty())
    return Comparator { a, b -> compareValuesBy(b, a, *selectors) }
}