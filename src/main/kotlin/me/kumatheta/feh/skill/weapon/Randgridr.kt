package me.kumatheta.feh.skill.weapon

import me.kumatheta.feh.BowB
import me.kumatheta.feh.MoveType
import me.kumatheta.feh.Stat
import me.kumatheta.feh.skill.*

private val FOE_EFFECT = combatStartSkill(Stat(atk = -6, def = -6)).toInCombatStatPassive()

val Randgridr = BasicWeapon(
    BowB, BasicSkill(
        extraStat = weaponStat(17),
        effectiveAgainstMoveType = setOf(MoveType.FLYING, MoveType.ARMORED),
        neutralizePenalty = { combatStatus ->
            if (combatStatus.foe.currentHp == combatStatus.foe.maxHp) {
                Stat.ZERO
            } else {
                null
            }
        },
        foeEffect = { combatStatus ->
            if (combatStatus.foe.currentHp == combatStatus.foe.maxHp) {
                FOE_EFFECT
            } else {
                null
            }
        }
    )
)