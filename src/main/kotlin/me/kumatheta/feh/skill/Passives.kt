package me.kumatheta.feh.skill

import me.kumatheta.feh.*
import me.kumatheta.feh.skill.assist.movement.HitAndRunEffect
import me.kumatheta.feh.skill.assist.movement.SwapEffect
import me.kumatheta.feh.skill.effect.*
import me.kumatheta.feh.skill.effect.incombatstat.*
import me.kumatheta.feh.skill.effect.startofturn.*
import me.kumatheta.feh.skill.effect.supportincombat.closeGuard
import me.kumatheta.feh.skill.effect.supportincombat.distantGuard
import me.kumatheta.feh.skill.effect.supportincombat.drive
import me.kumatheta.feh.skill.effect.supportincombat.spur
import me.kumatheta.feh.skill.effect.teleport.Aerobatics3
import me.kumatheta.feh.skill.effect.teleport.FlierFormation3
import me.kumatheta.feh.skill.passive.*

val ALL_PASSIVES = sequenceOf<Pair<String, Passive>>(
    "Upheaval" to Upheaval,
    "Close Counter" to CounterIgnoreRange,
    "Distant Counter" to CounterIgnoreRange,

    "Bold Fighter 3" to BoldFighter3,

    "Seal Atk/Def 2" to seal(Stat(def = -5, atk = -5)),

    "Atk/Def Bond 1" to bond(Stat(atk = 3, def = 3)),
    "Atk/Def Bond 2" to bond(Stat(atk = 4, def = 4)),
    "Atk/Def Bond 3" to bond(Stat(atk = 5, def = 5)),
    "Atk/Res Bond 1" to bond(Stat(atk = 3, res = 3)),
    "Atk/Res Bond 2" to bond(Stat(atk = 4, res = 4)),
    "Atk/Res Bond 3" to bond(Stat(atk = 5, res = 5)),
    "Atk/Spd Bond 1" to bond(Stat(atk = 3, spd = 3)),
    "Atk/Spd Bond 2" to bond(Stat(atk = 4, spd = 4)),
    "Atk/Spd Bond 3" to bond(Stat(atk = 5, spd = 5)),
    "Spd/Res Bond 1" to bond(Stat(spd = 3, res = 3)),

    "Atk/Spd Solo 3" to solo(Stat(atk = 6, spd = 6)),
    "Atk/Def Solo 3" to solo(Stat(atk = 6, def = 6)),
    "Spd/Def Solo 3" to solo(Stat(spd = 6, def = 6)),
    "Def/Res Solo 3" to solo(Stat(def = 6, res = 6)),

    "Death Blow 3" to blow(Stat(atk = 6)),
    "Darting Blow 3" to blow(Stat(spd = 6)),
    "Armored Blow 3" to blow(Stat(def = 6)),
    "Swift Sparrow 2" to blow(Stat(atk = 4, spd = 4)),
    "Sturdy Blow 2" to blow(Stat(atk = 4, def = 4)),
    "Mirror Strike 2" to blow(Stat(atk = 4, res = 4)),
    "Swift Strike 2" to blow(Stat(spd = 4, def= 4)),

    "Fierce Stance 3" to stance(Stat(atk = 6)),
    "Steady Stance 3" to stance(Stat(def = 6)),
    "Mirror Stance 2" to stance(Stat(atk = 4, res = 4)),
    "Steady Posture 2" to stance(Stat(spd = 4, def = 4)),
    "Bracing Stance 2" to stance(Stat(def = 4, res = 4)),
    "Warding Breath" to WardingBreath,
    "Guidance 3" to Guidance3,

    "Spd Smoke 3" to smoke(Stat(spd = -7)),
    "Def Smoke 3" to smoke(Stat(def = -7)),
    "Res Smoke 3" to smoke(Stat(res = -7)),

    "Spd Tactics 3" to tactics(Stat(spd = 6)),
    "Def Tactics 3" to tactics(Stat(def = 6)),
    "Atk Tactics 3" to tactics(Stat(atk = 6)),
    "Panic Ploy 3" to ploy { self, foe ->
        if (foe.currentHp < self.currentHp) {
            foe.addNegativeStatus(NegativeStatus.PANIC)
        }
    },
    "Res Ploy 3" to resBasedPloy3(Stat(res = -5)),
    "Atk Ploy 3" to resBasedPloy3(Stat(atk = -5)),

    "Quick Riposte 3" to quickRiposte(70),
    "Quick Riposte 2" to quickRiposte(80),
    "Quick Riposte 1" to quickRiposte(90),
    "Sword Breaker 3" to breaker(Sword, 50),
    "Bow Breaker 3" to breaker(BowC, 50),
    "B Tome breaker 3" to breaker(MagicB, 50),
    "Vantage 3" to vantage(75),
    "Desperation 3" to desperation(75),
    "Wary Fighter 3" to waryFighter(50),
    "Armor March 3" to armorMarch3(),
    "Windsweep 3" to windsweep(1),

    "Sabotage Atk 3" to sabotage(Stat(atk = -7)),
    "Distant Def 3" to rangeDefStat(
        Stat(
            def = 6,
            res = 6
        )
    ),
    "Hone Atk 3" to hone(Stat(atk = 4)),
    "Fortify Res 4" to hone(Stat(res = 7)),
    "Fortify Fliers" to hone(Stat(def = 6, res = 6), MoveType.FLYING),
    "Wrathful Staff 3" to WrathfulStaff3,
    "Wrath 3" to wrath(75),
    "Time's Pulse 3" to TimePulse3,
    "Quickened Pulse" to QuickenedPulse,
    "Hardy Bearing 3" to HardyBearing3,
    "Lunge" to BasicSkill(postInitiateMovement = SwapEffect),
    "Hit and Run" to BasicSkill(postInitiateMovement = HitAndRunEffect),

    "Chill Def 3" to chill(Stat(def = -7)) {
        it.visibleStat.def
    },
    "Chill Res 3" to chill(Stat(res = -7)) {
        it.visibleStat.res
    },
    "Odd Atk Wave 3" to wave(
        Stat(atk = 6),
        oddTurn = true
    ),
    "Odd Spd Wave 3" to wave(
        Stat(spd = 6),
        oddTurn = true
    ),
    "Odd Def Wave 3" to wave(
        Stat(def = 6),
        oddTurn = true
    ),
    "Odd Res Wave 3" to wave(
        Stat(res = 6),
        oddTurn = true
    ),
    "Even Atk Wave 3" to wave(
        Stat(atk = 6),
        oddTurn = false
    ),
    "Even Spd Wave 3" to wave(
        Stat(spd = 6),
        oddTurn = false
    ),
    "Even Def Wave 3" to wave(
        Stat(def = 6),
        oddTurn = false
    ),
    "Even Res Wave 3" to wave(
        Stat(res = 6),
        oddTurn = false
    ),
    "Infantry Pulse 3" to infantryPulse(minDiff = 1),
    "Threaten Def 3" to threaten(Stat(def = -5)),

    "Pass 3" to pass(percentageHp = 25),
    "Obstruct 3" to obstruct(percentageHp = 25),
    "HP+5" to Stat(hp = 5).toExtraStatPassive(),
    "Res+3" to Stat(res = 3).toExtraStatPassive(),
    "Atk/Res 2" to Stat(atk = 2, res = 2).toExtraStatPassive(),
    "Fortress Res 3" to Stat(atk = -3, res = 5).toExtraStatPassive(),
    "Fortress Def 3" to Stat(atk = -3, def = 5).toExtraStatPassive(),
    "Life and Death 3" to Stat(atk = 5, spd = 5, def = -5, res = -5).toExtraStatPassive(),
    "B Duel Flying 3" to Stat(hp = 5).toExtraStatPassive(),
    "Atk/Spd 2" to Stat(atk = 2, spd = 2).toExtraStatPassive(),
    "Spur Atk/Spd 2" to spur(Stat(atk = 2, spd = 2)),
    "Spur Def/Res 2" to spur(Stat(def = 3, res = 3)),
    "Spur Def 3" to spur(Stat(def = 4)),
    "Spur Res 3" to spur(Stat(res = 4)),
    "Spur Spd 1" to spur(Stat(spd = 2)),
    "Earth Boost 3" to boost(Stat(def = 6)),
    "Wind Boost 3" to boost(Stat(spd = 6)),
    "Fire Boost 3" to boost(Stat(atk = 6)),
    "Distant Guard 3" to distantGuard(Stat(def = 4, res = 4)),
    "Close Guard 3" to closeGuard(Stat(def = 4, res = 4)),
    "Heavy Blade 3" to HeavyBlade3,
    "Flashing Blade 3" to FlashingBlade3,
    "Pulse Smoke 3" to pulseSmoke3,
    "Renewal 3" to renewal(2),

    "Live to Serve 3" to LiveToServe3,
    "Null Follow-up 3" to NullFollowUp3,
    "Guard 3" to Guard3,
    "Triangle Adept 3" to BasicSkill(triangleAdept = combatSkill(20)),
    "Poison Strike 3" to poisonStrike(10),
    "Fury 3" to Fury3,
    "Dull Close 3" to DullClose3,
    "Dull Range 3" to DullRange3,
    "Iote's Shield" to MoveType.CAVALRY.toNeutralizeEffectivePassive(),
    "Def Feint 3" to DefFeint3,
    "Mystic Boost 3" to MysticBoost3,
    "Sparkling Boost" to SparklingBoost,
    "Shield Pulse 3" to ShieldPulse3,
    "Drive Atk 2" to drive(Stat(atk = 3)),
    "Drive Spd 2" to drive(Stat(spd = 3)),
    "Drive Def 2" to drive(Stat(def = 3)),
    "Drive Res 2" to drive(Stat(res = 3)),
    "Goad Cavalry" to drive(
        Stat(atk = 4, spd = 4),
        MoveType.CAVALRY
    ),
    "Aerobatics 3" to Aerobatics3,
    "Flier Formation 3" to FlierFormation3,
    "Air Orders 3" to airOrder3,
    "Atk Opening 3" to opening(Stat(atk = 6)) {
        it.startOfTurnStat.atk
    }

).toSkillMap()

//fun CombatStartSkill<Boolean>.toVantagePassive(): Passive = BasicSkill(vantage = this)
//fun CombatStartSkill<Boolean>.toDesperationPassive(): Passive = BasicSkill(desperation = this)
//fun CombatStartSkill<Int>.toFollowUpPassive(): Passive = BasicSkill(followUp = this)
//fun InCombatSkill<Int>.toTriangleAdeptPassive(): Passive = BasicSkill(triangleAdept = this)
//fun CombatStartSkill<Int>.toCounterPassive(): Passive = BasicSkill(counter = this)
//fun SupportCombatEffect.toSupportInCombatBuffPassive(): Passive = BasicSkill(supportInCombatBuff = this)
//fun MapSkillMethod<Unit>.toStartOfTurnPassive(): Passive = BasicSkill(startOfTurn = this)
fun Stat.toExtraStatPassive(): Passive = BasicSkill(extraStat = this)

//fun CombatStartSkill<Stat>.toInCombatStatPassive(): Passive = BasicSkill(inCombatStat = this)
//fun CombatStartSkill<Stat>.toNeutralizeBonusPassive(): Passive = BasicSkill(neutralizeBonus = this)
//fun CombatEndSkill.toCombatEndPassive(): Passive = BasicSkill(combatEnd = this)
//fun MapSkillMethod<Sequence<Position>>.toTeleportPassive(): Passive = BasicSkill(teleport = this)
//fun InCombatSkill<CooldownChange<Int>>.toCooldownBuff(): Passive = BasicSkill(cooldownBuff = this)
//fun InCombatSkill<CooldownChange<Int>>.toCooldownDebuff(): Passive = BasicSkill(cooldownDebuff = this)
fun Set<MoveType>.toNeutralizeEffectivePassive(): Passive = BasicSkill(neutralizeEffectiveMoveType = this)

fun MoveType.toNeutralizeEffectivePassive(): Passive = setOf(this).toNeutralizeEffectivePassive()
