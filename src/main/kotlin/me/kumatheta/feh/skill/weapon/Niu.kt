package me.kumatheta.feh.skill.weapon

import me.kumatheta.feh.Stat
import me.kumatheta.feh.Sword
import me.kumatheta.feh.skill.CombatStatus
import me.kumatheta.feh.skill.InCombatStat
import me.kumatheta.feh.skill.basic
import me.kumatheta.feh.skill.effect.ExtraInCombatStatEffect
import me.kumatheta.feh.skill.plus

val Niu = Sword.basic(16, spd = 3) + object : ExtraInCombatStatEffect {
    override fun apply(combatStatus: CombatStatus<InCombatStat>): Stat {
        val bonus = combatStatus.foe.bonus.totalExceptHp / 2
        return Stat(atk = bonus, spd = bonus, def = bonus, res = bonus)
    }
}