package me.kumatheta.feh.skill.weapon

import me.kumatheta.feh.BasicWeapon
import me.kumatheta.feh.BowB
import me.kumatheta.feh.CombatStartSkill
import me.kumatheta.feh.MoveType
import me.kumatheta.feh.Skill
import me.kumatheta.feh.Stat
import me.kumatheta.feh.combatStartSkill
import me.kumatheta.feh.skill.passive.toInCombatStatPassive

private val FOE_EFFECT = combatStartSkill(Stat(atk = -6, def = -6)).toInCombatStatPassive()

object Randgridr : BasicWeapon(BowB, 14) {
    override val extraStat: Stat = Stat(atk = 3)
    override val effectiveAgainstMoveType: Set<MoveType>? = setOf(MoveType.FLYING, MoveType.ARMORED)
    override val neutralizePenalty: CombatStartSkill<Stat?>? = {combatStatus ->
        if (combatStatus.foe.currentHp == combatStatus.foe.maxHp) {
            Stat.ZERO
        } else {
            null
        }
    }
    override val foeEffect: CombatStartSkill<Skill?>? = {combatStatus ->
        if (combatStatus.foe.currentHp == combatStatus.foe.maxHp) {
            FOE_EFFECT
        } else {
            null
        }
    }
}