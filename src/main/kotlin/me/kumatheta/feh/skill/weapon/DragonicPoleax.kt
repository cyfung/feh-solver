package me.kumatheta.feh.skill.weapon

import me.kumatheta.feh.Axe
import me.kumatheta.feh.BasicWeapon
import me.kumatheta.feh.InCombatSkill
import me.kumatheta.feh.MapSkillMethod
import me.kumatheta.feh.Stat
import me.kumatheta.feh.combatSkill
import me.kumatheta.feh.skill.effect.tactics

object DraconicPoleaxEff : BasicWeapon(Axe, 16) {
    override val extraStat: Stat = Stat(hp = 3)

    override val triangleAdept: InCombatSkill<Int>? = combatSkill(20)

    override val startOfTurn: MapSkillMethod<Unit>? = tactics(Stat(res = 6))
}