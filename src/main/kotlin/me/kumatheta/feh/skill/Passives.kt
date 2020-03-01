package me.kumatheta.feh.skill

import me.kumatheta.feh.*
import me.kumatheta.feh.skill.assist.movement.SwapEffect
import me.kumatheta.feh.skill.effect.*
import me.kumatheta.feh.skill.passive.*

val ALL_PASSIVES = sequenceOf(
    "Close Counter" to CounterIgnoreRange,
    "Distant Counter" to CounterIgnoreRange,

    "Seal Atk/Def 2" to seal(Stat(def = -5, atk = -5)).toCombatEndPassive(),

    "Atk/Def Bond 1" to bond(Stat(atk = 3, def = 3)).toInCombatStatPassive(),
    "Atk/Def Bond 2" to bond(Stat(atk = 4, def = 4)).toInCombatStatPassive(),
    "Atk/Def Bond 3" to bond(Stat(atk = 5, def = 5)).toInCombatStatPassive(),
    "Atk/Res Bond 1" to bond(Stat(atk = 3, res = 3)).toInCombatStatPassive(),
    "Atk/Res Bond 2" to bond(Stat(atk = 4, res = 4)).toInCombatStatPassive(),
    "Atk/Res Bond 3" to bond(Stat(atk = 5, res = 5)).toInCombatStatPassive(),

    "Atk/Spd Solo 3" to solo(Stat(atk = 6, spd = 6)).toInCombatStatPassive(),
    "Atk/Def Solo 3" to solo(Stat(atk = 6, def = 6)).toInCombatStatPassive(),
    "Spd/Def Solo 3" to solo(Stat(spd = 6, def = 6)).toInCombatStatPassive(),
    "Def/Res Solo 3" to solo(Stat(def = 6, res = 6)).toInCombatStatPassive(),

    "Death Blow 3" to blow(Stat(atk = 6)).toInCombatStatPassive(),
    "Swift Sparrow 2" to blow(Stat(atk = 4, spd = 4)).toInCombatStatPassive(),
    "Sturdy Blow 2" to blow(Stat(atk = 4, def = 4)).toInCombatStatPassive(),
    "Mirror Strike 2" to blow(Stat(atk = 4, res = 4)).toInCombatStatPassive(),

    "Fierce Stance 3" to stance(Stat(atk = 6)).toInCombatStatPassive(),
    "Mirror Stance 2" to stance(Stat(atk = 4, res = 4)).toInCombatStatPassive(),
    "Steady Posture 2" to stance(Stat(spd = 4, def = 4)).toInCombatStatPassive(),
    "Warding Breath" to WardingBreath,

    "Spd Smoke 3" to smoke(Stat(spd = -7)).toCombatEndPassive(),

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
    "B Tome breaker 3" to breaker(MagicB, 50).toFollowUpPassive(),
    "Vantage 3" to belowThreshold(75).toVantagePassive(),
    "Desperation 3" to belowThreshold(75).toDesperationPassive(),
    "Wary Fighter 3" to WaryFighter(50),
    "Armor March 3" to armorMarch3().toStartOfTurnPassive(),

    "Sabotage Atk 3" to sabotage(Stat(atk = -7)).toStartOfTurnPassive(),
    "Distant Def 3" to rangeDefStat(Stat(def = 6, res = 6)).toInCombatStatPassive(),
    "Hone Atk 3" to hone(Stat(atk = 4)).toStartOfTurnPassive(),
    "Fortify Res 4" to hone(Stat(res = 7)).toStartOfTurnPassive(),
    "Wrathful Staff 3" to WrathfulStaff3,
    "Wrath 3" to Wrath(75),
    "Time's Pulse 3" to TimePulse3,
    "Lunge" to BasicSkill(postInitiateMovement = SwapEffect),

    "Chill Def 3" to chill(Stat(def = -7)) {
        it.visibleStat.def
    }.toStartOfTurnPassive(),
    "Chill Res 3" to chill(Stat(res = -7)) {
        it.visibleStat.res
    }.toStartOfTurnPassive(),
    "Odd Atk Wave 3" to wave(Stat(atk = 6), oddTurn = true).toStartOfTurnPassive(),
    "Odd Spd Wave 3" to wave(Stat(spd = 6), oddTurn = true).toStartOfTurnPassive(),
    "Odd Def Wave 3" to wave(Stat(def = 6), oddTurn = true).toStartOfTurnPassive(),
    "Odd Res Wave 3" to wave(Stat(res = 6), oddTurn = true).toStartOfTurnPassive(),
    "Even Atk Wave 3" to wave(Stat(atk = 6), oddTurn = false).toStartOfTurnPassive(),
    "Even Spd Wave 3" to wave(Stat(spd = 6), oddTurn = false).toStartOfTurnPassive(),
    "Even Def Wave 3" to wave(Stat(def = 6), oddTurn = false).toStartOfTurnPassive(),
    "Even Res Wave 3" to wave(Stat(res = 6), oddTurn = false).toStartOfTurnPassive(),
    "Infantry Pulse 3" to infantryPulse(minDiff = 1).toStartOfTurnPassive(),
    "Threaten Def 3" to threaten(Stat(def = -5)).toStartOfTurnPassive(),

    "Pass 3" to Pass(percentageHp = 25),
    "HP+5" to Stat(hp = 5).toExtraStatPassive(),
    "Res+3" to Stat(res = 3).toExtraStatPassive(),
    "Atk/Res 2" to Stat(atk = 2, res = 2).toExtraStatPassive(),
    "Fortress Res 3" to Stat(atk = -3, res = 5).toExtraStatPassive(),
    "Fortress Def 3" to Stat(atk = -3, def = 5).toExtraStatPassive(),
    "Life and Death 3" to Stat(atk = 5, spd = 5, def = -5, res = -5).toExtraStatPassive(),
    "B Duel Flying 3" to Stat(hp = 5).toExtraStatPassive(),
    "Atk/Spd 2" to Stat(atk = 2, spd = 2).toExtraStatPassive(),
    "Spur Atk/Spd 2" to spur(Stat(atk = 2, spd = 2)).toSupportInCombatBuffPassive(),
    "Spur Def/Res 2" to spur(Stat(def = 3, res = 3)).toSupportInCombatBuffPassive(),
    "Spur Def 3" to spur(Stat(def = 4)).toSupportInCombatBuffPassive(),
    "Spur Res 3" to spur(Stat(res = 4)).toSupportInCombatBuffPassive(),
    "Spur Spd 1" to spur(Stat(spd = 2)).toSupportInCombatBuffPassive(),
    "Earth Boost 3" to boost(Stat(def = 6)).toInCombatStatPassive(),
    "Wind Boost 3" to boost(Stat(spd = 6)).toInCombatStatPassive(),
    "Fire Boost 3" to boost(Stat(atk = 6)).toInCombatStatPassive(),
    "Distant Guard 3" to distantGuard(Stat(def=4, res=4)).toSupportInCombatBuffPassive(),
    "Heavy Blade 3" to HeavyBlade3,
    "Pulse Smoke 3" to pulseSmoke3.toCombatEndPassive(),
    "Renewal 3" to Renewal3,

    "Live to Serve 3" to LiveToServe3,
    "Null Follow-up 3" to NullFollowUp3,
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
    "Drive Atk 2" to Drive(Stat(atk = 3)),
    "Drive Spd 2" to Drive(Stat(spd = 3)),
    "Drive Res 2" to Drive(Stat(res = 3)),
    "Goad Cavalry" to GoadCavalry,
    "Flier Formation 3" to FlierFormation3,
    "Air Orders 3" to airOrder3.toStartOfTurnPassive(),
    "Atk Opening 3" to opening(Stat(atk = 6)) {
        it.startOfTurnStat.atk
    }.toStartOfTurnPassive()

).toSkillMap()

fun CombatStartSkill<Boolean>.toVantagePassive(): Passive = BasicSkill(vantage = this)
fun CombatStartSkill<Boolean>.toDesperationPassive(): Passive = BasicSkill(desperation = this)
fun CombatStartSkill<Int>.toFollowUpPassive(): Passive = BasicSkill(followUp = this)
fun CombatStartSkill<Int>.toCounterPassive(): Passive = BasicSkill(counter = this)
fun SupportCombatEffect.toSupportInCombatBuffPassive(): Passive = BasicSkill(supportInCombatBuff = this)
fun MapSkillMethod<Unit>.toStartOfTurnPassive(): Passive = BasicSkill(startOfTurn = this)
fun Stat.toExtraStatPassive(): Passive = BasicSkill(extraStat = this)
fun CombatStartSkill<Stat>.toInCombatStatPassive(): Passive = BasicSkill(inCombatStat = this)
fun CombatStartSkill<Stat>.toNeutralizeBonusPassive(): Passive = BasicSkill(neutralizeBonus = this)
fun CombatEndSkill.toCombatEndPassive() = BasicSkill(combatEnd = this)