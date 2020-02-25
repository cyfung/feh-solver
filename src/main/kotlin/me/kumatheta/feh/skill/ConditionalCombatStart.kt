package me.kumatheta.feh.skill

import me.kumatheta.feh.CombatStartSkill

inline fun <T> CombatStartSkill<Boolean>.conditional(
    crossinline success: CombatStartSkill<T>,
    crossinline fail: CombatStartSkill<T>
): CombatStartSkill<T> {
    return {
        if (this(it)) {
            success(it)
        } else {
            fail(it)
        }
    }
}
