package me.kumatheta.feh.skill.weapon

import me.kumatheta.feh.Stat
import me.kumatheta.feh.WeaponType
import me.kumatheta.feh.skill.BasicSkill
import me.kumatheta.feh.skill.BasicWeapon
import me.kumatheta.feh.skill.effect.aoeDebuff
import me.kumatheta.feh.skill.weaponStat

fun WeaponType.darkBreathPlusRefine(might: Int, hp: Int = 0, spd: Int = 0, def: Int = 0, res: Int = 0): BasicWeapon =
    BasicWeapon(
        this, BasicSkill(
            extraStat = weaponStat(might = might, hp = hp, spd = spd, def = def, res = res),
            combatEnd = aoeDebuff(2, true, Stat(atk = -7, spd = -7)),
            adaptiveDamage = DragonAdaptive.adaptiveDamage!!
        )
    )