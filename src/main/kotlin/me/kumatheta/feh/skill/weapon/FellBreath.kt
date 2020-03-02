package me.kumatheta.feh.skill.weapon

import me.kumatheta.feh.DragonC
import me.kumatheta.feh.Stat
import me.kumatheta.feh.skill.*

private val BUFF = Stat(atk = 6, res = 6)

private val FOE_EFFECT = BasicSkill(followUp = combatSkill(-1))

val FellBreath = BasicWeapon(
    DragonC, BasicSkill(
        extraStat = weaponStat(19),
        adaptiveDamage = DragonAdaptive.adaptiveDamage!!,
        inCombatStat = {
            if (it.foe.currentHp < it.foe.maxHp) {
                BUFF
            } else {
                Stat.ZERO
            }
        },
        foeEffect = {
            if (it.foe.currentHp < it.foe.maxHp) {
                FOE_EFFECT
            } else {
                null
            }
        }
    )
)