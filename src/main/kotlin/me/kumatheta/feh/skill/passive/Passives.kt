package me.kumatheta.feh.skill.passive

import me.kumatheta.feh.CombatStartSkill
import me.kumatheta.feh.MapSkillMethod
import me.kumatheta.feh.MapSkillWithTarget
import me.kumatheta.feh.NegativeStatus
import me.kumatheta.feh.Passive
import me.kumatheta.feh.Skill
import me.kumatheta.feh.Stat
import me.kumatheta.feh.skill.BlowOrStance
import me.kumatheta.feh.skill.Ploy
import me.kumatheta.feh.skill.Tactics
import me.kumatheta.feh.skill.toSkillMap

val ALL_PASSIVES = sequenceOf(
    "Close Counter" to CounterIgnoreRange,
    "Distant Counter" to CounterIgnoreRange,

    "Atk/Def Bond 1" to Bond(Stat(atk = 3, def = 3)).toInCombatStatPassive(),
    "Atk/Def Bond 2" to Bond(Stat(atk = 4, def = 4)).toInCombatStatPassive(),
    "Atk/Def Bond 3" to Bond(Stat(atk = 5, def = 5)).toInCombatStatPassive(),
    "Atk/Res Bond 1" to Bond(Stat(atk = 3, res = 3)).toInCombatStatPassive(),
    "Atk/Res Bond 2" to Bond(Stat(atk = 4, res = 4)).toInCombatStatPassive(),
    "Atk/Res Bond 3" to Bond(Stat(atk = 5, res = 5)).toInCombatStatPassive(),

    "Death Blow 3" to BlowOrStance(Stat(atk = 6), Stat.ZERO).toInCombatStatPassive(),
    "Swift Sparrow 2" to BlowOrStance(Stat(atk = 4, spd = 4), Stat.ZERO).toInCombatStatPassive(),

    "Spd Tactics 3" to Tactics(Stat(spd = 6)).toStartOfTurnPassive(),
    "Def Tactics 3" to Tactics(Stat(def = 6)).toStartOfTurnPassive(),
    "Atk Tactics 3" to Tactics(Stat(atk = 6)).toStartOfTurnPassive(),
    "Panic Ploy 3" to Ploy({ self, foe ->
        foe.currentHp < self.currentHp
    }) {
        it.addNegativeStatus(NegativeStatus.PANIC)
    }.toStartOfTurnPassive(),
    "Res Ploy 3" to Ploy({ self, foe ->
        foe.stat.res < self.stat.res
    }) {
        it.applyDebuff(Stat(res = -5))
    }.toStartOfTurnPassive(),

    "Res 3" to Stat(res = 3).toExtraStatPassive(),
    "Spur Def/Res 2" to Spur(Stat(def = 3, res = 3)).toSupportInCombatBuffPassive(),
    "Spur Spd 1" to Spur(Stat(spd = 2)).toSupportInCombatBuffPassive(),

    "Guard 3" to Guard3,
    "Triangle Adept 3" to TriangleAdept3,
    "Poison Strike 3" to PoisonStrike3,
    "Fury 3" to Fury3,
    "Aerobatics 3" to Aerobatics3,
    "Dull Close 3" to DullClose3,
    "Sabotage Atk 3" to SabotageAtk3,
    "Iote's Shield" to IoteShield,
    "Def Feint 3" to DefFeint3,
    "Mystic Boost 3" to MysticBoost3,
    "Sparkling Boost" to SparklingBoost,
    "Shield Pulse 3" to ShieldPulse3,
    "Drive Atk 2" to DriveAtk2,
    "Quick Riposte 3" to QuickRiposte3,
    "Vantage 3" to Vantage3,
    "Goad Cavalry" to GoadCavalry,
    "Hone Atk 3" to HoneAtk3

).toSkillMap()

class SupportInCombatBuffPassive(supportInCombatBuff: MapSkillWithTarget<Skill?>): Passive {
    override val supportInCombatBuff: MapSkillWithTarget<Skill?>? = supportInCombatBuff
}

fun MapSkillWithTarget<Skill?>.toSupportInCombatBuffPassive(): SupportInCombatBuffPassive {
    return SupportInCombatBuffPassive(this)
}

class StartOfTurnPassive(mapSkillMethod: MapSkillMethod<Unit>) : Passive {
    override val startOfTurn: MapSkillMethod<Unit>? = mapSkillMethod
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