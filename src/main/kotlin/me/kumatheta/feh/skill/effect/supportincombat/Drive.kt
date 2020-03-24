package me.kumatheta.feh.skill.effect.supportincombat

import me.kumatheta.feh.AttackerDefenderPair
import me.kumatheta.feh.MoveType
import me.kumatheta.feh.Stat
import me.kumatheta.feh.skill.effect.InCombatSkillEffect
import me.kumatheta.feh.skill.effect.InCombatSupport
import me.kumatheta.feh.skill.effect.InCombatSupportInput
import me.kumatheta.feh.skill.effect.SkillEffect
import me.kumatheta.feh.skill.effect.emptyAttackerDefenderSequences
import me.kumatheta.feh.skill.effect.forAlly
import me.kumatheta.feh.skill.toInCombatStatEffect

fun drive(buff: InCombatSkillEffect, moveType: MoveType?) =
    if (moveType == null) {
        object : InCombatSupport {
            override fun getSupportSkills(inCombatSupportInput: InCombatSupportInput): AttackerDefenderPair<Sequence<InCombatSkillEffect>> {
                return if (inCombatSupportInput.targetAlly.position.distanceTo(inCombatSupportInput.self.position) <= 2) {
                    inCombatSupportInput.forAlly(buff)
                } else {
                    emptyAttackerDefenderSequences()
                }
            }
        }
    } else {
        object : InCombatSupport {
            override fun getSupportSkills(inCombatSupportInput: InCombatSupportInput): AttackerDefenderPair<Sequence<InCombatSkillEffect>> {
                return if (inCombatSupportInput.targetAlly.moveType == moveType &&
                    inCombatSupportInput.targetAlly.position.distanceTo(inCombatSupportInput.self.position) <= 2
                ) {
                    inCombatSupportInput.forAlly(buff)
                } else {
                    emptyAttackerDefenderSequences()
                }
            }
        }
    }

fun drive(buff: Stat, moveType: MoveType? = null): InCombatSupport {
    return drive(buff.toInCombatStatEffect(), moveType)
}

fun drive(
    atk: Int = 0,
    spd: Int = 0,
    def: Int = 0,
    res: Int = 0,
    moveType: MoveType? = null
) = drive(Stat(atk = atk, spd = spd, def = def, res = res), moveType)
