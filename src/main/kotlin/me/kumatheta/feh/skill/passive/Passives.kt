package me.kumatheta.feh.skill.passive

import me.kumatheta.feh.BowC
import me.kumatheta.feh.CombatStartSkill
import me.kumatheta.feh.InCombatSkill
import me.kumatheta.feh.MapSkillMethod
import me.kumatheta.feh.MapSkillWithTarget
import me.kumatheta.feh.NegativeStatus
import me.kumatheta.feh.Passive
import me.kumatheta.feh.Skill
import me.kumatheta.feh.Stat
import me.kumatheta.feh.skill.airOrder3
import me.kumatheta.feh.skill.belowThreshold
import me.kumatheta.feh.skill.blow
import me.kumatheta.feh.skill.bond
import me.kumatheta.feh.skill.breaker
import me.kumatheta.feh.skill.hone
import me.kumatheta.feh.skill.opening
import me.kumatheta.feh.skill.ploy
import me.kumatheta.feh.skill.resBasedPloy3
import me.kumatheta.feh.skill.stance
import me.kumatheta.feh.skill.tactics
import me.kumatheta.feh.skill.toSkillMap

val ALL_PASSIVES = sequenceOf(
    "Close Counter" to CounterIgnoreRange,
    "Distant Counter" to CounterIgnoreRange,

    "Atk/Def Bond 1" to bond(Stat(atk = 3, def = 3)).toInCombatStatPassive(),
    "Atk/Def Bond 2" to bond(Stat(atk = 4, def = 4)).toInCombatStatPassive(),
    "Atk/Def Bond 3" to bond(Stat(atk = 5, def = 5)).toInCombatStatPassive(),
    "Atk/Res Bond 1" to bond(Stat(atk = 3, res = 3)).toInCombatStatPassive(),
    "Atk/Res Bond 2" to bond(Stat(atk = 4, res = 4)).toInCombatStatPassive(),
    "Atk/Res Bond 3" to bond(Stat(atk = 5, res = 5)).toInCombatStatPassive(),

    "Death Blow 3" to blow(Stat(atk = 6)).toInCombatStatPassive(),
    "Swift Sparrow 2" to blow(Stat(atk = 4, spd = 4)).toInCombatStatPassive(),
    "Sturdy Blow 2" to blow(Stat(atk = 4, def = 4)).toInCombatStatPassive(),
    "Mirror Strike 2" to blow(Stat(atk = 4, res = 4)).toInCombatStatPassive(),

    "Mirror Stance 2" to stance(Stat(atk = 4, res = 4)).toInCombatStatPassive(),
    "Steady Posture 2" to stance(Stat(spd = 4, def = 4)).toInCombatStatPassive(),

    "Spd Tactics 3" to tactics(Stat(spd = 6)).toStartOfTurnPassive(),
    "Def Tactics 3" to tactics(Stat(def = 6)).toStartOfTurnPassive(),
    "Atk Tactics 3" to tactics(Stat(atk = 6)).toStartOfTurnPassive(),
    "Panic Ploy 3" to ploy { self, foe ->
        if (foe.currentHp < self.currentHp) {
            foe.addNegativeStatus(NegativeStatus.PANIC)
        }
    }.toStartOfTurnPassive(),
    "Res Ploy 3" to resBasedPloy3(Stat(res = -5)).toStartOfTurnPassive(),
    "Atk Ploy 3" to resBasedPloy3(Stat(atk = -5)).toStartOfTurnPassive(),

    "Quick Riposte 3" to quickRiposte(70).toFollowUpPassive(),
    "Quick Riposte 2" to quickRiposte(80).toFollowUpPassive(),
    "Quick Riposte 1" to quickRiposte(90).toFollowUpPassive(),
    "Bow Breaker 3" to breaker(BowC, 50).toFollowUpPassive(),
    "Vantage 3" to belowThreshold(75).toVantagePassive(),
    "Desperation 3" to belowThreshold(75).toDesperationPassive(),

    "Sabotage Atk 3" to sabotage(Stat(atk = -7)).toStartOfTurnPassive(),
    "Hone Atk 3" to hone(Stat(atk = 4)).toStartOfTurnPassive(),
    "Fortify Res 4" to hone(Stat(res = 7)).toStartOfTurnPassive(),


    "Res 3" to Stat(res = 3).toExtraStatPassive(),
    "B Duel Flying 3" to Stat(hp = 5).toExtraStatPassive(),
    "Atk/Spd 2" to Stat(atk = 2, spd = 2).toExtraStatPassive(),
    "Spur Atk/Spd 2" to Spur(Stat(atk = 2, spd = 2)).toSupportInCombatBuffPassive(),
    "Spur Def/Res 2" to Spur(Stat(def = 3, res = 3)).toSupportInCombatBuffPassive(),
    "Spur Spd 1" to Spur(Stat(spd = 2)).toSupportInCombatBuffPassive(),

    "Guard 3" to Guard3,
    "Triangle Adept 3" to TriangleAdept3,
    "Poison Strike 3" to PoisonStrike3,
    "Fury 3" to Fury3,
    "Aerobatics 3" to Aerobatics3,
    "Dull Close 3" to DullClose3,
    "Dull Range 3" to DullRange3,
    "Iote's Shield" to IoteShield,
    "Def Feint 3" to DefFeint3,
    "Mystic Boost 3" to MysticBoost3,
    "Sparkling Boost" to SparklingBoost,
    "Shield Pulse 3" to ShieldPulse3,
    "Drive Atk 2" to DriveAtk2,
    "Goad Cavalry" to GoadCavalry,
    "Flier Formation 3" to FlierFormation3,
    "Air Orders 3" to airOrder3.toStartOfTurnPassive(),
    "Atk Opening 3" to opening(Stat(atk = 6)) {
        it.startOfTurnStat.atk
    }.toStartOfTurnPassive()

).toSkillMap()

class VantagePassive(vantage: InCombatSkill<Boolean>) : Passive {
    override val vantage: InCombatSkill<Boolean>? = vantage
}

fun InCombatSkill<Boolean>.toVantagePassive(): VantagePassive {
    return VantagePassive(this)
}

class DesperationPassive(desperation: InCombatSkill<Boolean>) : Passive {
    override val desperation: InCombatSkill<Boolean>? = desperation
}

fun InCombatSkill<Boolean>.toDesperationPassive(): DesperationPassive {
    return DesperationPassive(this)
}


class FollowUpPassive(followUp: InCombatSkill<Int>) : Passive {
    override val followUp: InCombatSkill<Int>? = followUp
}

fun InCombatSkill<Int>.toFollowUpPassive(): FollowUpPassive {
    return FollowUpPassive(this)
}

class SupportInCombatBuffPassive(supportInCombatBuff: MapSkillWithTarget<Skill?>) : Passive {
    override val supportInCombatBuff: MapSkillWithTarget<Skill?>? = supportInCombatBuff
}

fun MapSkillWithTarget<Skill?>.toSupportInCombatBuffPassive(): SupportInCombatBuffPassive {
    return SupportInCombatBuffPassive(this)
}

class StartOfTurnPassive(startOfTurn: MapSkillMethod<Unit>) : Passive {
    override val startOfTurn: MapSkillMethod<Unit>? = startOfTurn
}

fun MapSkillMethod<Unit>.toStartOfTurnPassive(): StartOfTurnPassive {
    return StartOfTurnPassive(this)
}

class InCombatStatPassive(inCombatStat: CombatStartSkill<Stat>) : Passive {
    override val inCombatStat: CombatStartSkill<Stat>? = inCombatStat
}

class ExtraStatPassive(extraStat: Stat) : Passive {
    override val extraStat: Stat? = extraStat
}

fun Stat.toExtraStatPassive(): ExtraStatPassive {
    return ExtraStatPassive(this)
}

fun CombatStartSkill<Stat>.toInCombatStatPassive(): InCombatStatPassive {
    return InCombatStatPassive(this)
}