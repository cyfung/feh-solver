package me.kumatheta.feh.skill.weapon

import me.kumatheta.feh.DragonC
import me.kumatheta.feh.Stat
import me.kumatheta.feh.skill.BasicSkill
import me.kumatheta.feh.skill.BasicWeapon
import me.kumatheta.feh.skill.effect.incombatstat.stance
import me.kumatheta.feh.skill.weaponStat

val waterBreathPlusS = BasicWeapon(
    DragonC, BasicSkill(
        extraStat = weaponStat(might = 14, hp = 5, spd = 3),
        inCombatStat = stance(Stat(def = 4, res = 4)).inCombatStat!!,
        adaptiveDamage = DragonAdaptive.adaptiveDamage!!
    )
)