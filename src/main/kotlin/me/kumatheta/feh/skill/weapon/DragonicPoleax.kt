package me.kumatheta.feh.skill.weapon

import me.kumatheta.feh.Axe
import me.kumatheta.feh.Stat
import me.kumatheta.feh.skill.BasicSkill
import me.kumatheta.feh.skill.BasicWeapon
import me.kumatheta.feh.skill.combatSkill
import me.kumatheta.feh.skill.effect.tactics
import me.kumatheta.feh.skill.weaponStat

val DraconicPoleaxEff = BasicWeapon(
    Axe, BasicSkill(
        extraStat = weaponStat(16, hp = 3),
        triangleAdept = combatSkill(20),
        startOfTurn = tactics(Stat(res = 6))
    )
)