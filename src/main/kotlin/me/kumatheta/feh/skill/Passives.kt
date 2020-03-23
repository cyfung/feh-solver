package me.kumatheta.feh.skill

import me.kumatheta.feh.BowC
import me.kumatheta.feh.MagicB
import me.kumatheta.feh.MoveType
import me.kumatheta.feh.NegativeStatus
import me.kumatheta.feh.Stat
import me.kumatheta.feh.Sword
import me.kumatheta.feh.skill.assist.movement.HitAndRunEffect
import me.kumatheta.feh.skill.assist.movement.SwapEffect
import me.kumatheta.feh.skill.effect.CounterAnyRangeBasic
import me.kumatheta.feh.skill.effect.Desperation
import me.kumatheta.feh.skill.effect.DisablePriorityChangeBasic
import me.kumatheta.feh.skill.effect.NeutralizeEffectiveAgainstMovement
import me.kumatheta.feh.skill.effect.NeutralizeFollowUpBasic
import me.kumatheta.feh.skill.effect.Obstruct
import me.kumatheta.feh.skill.effect.Pass
import me.kumatheta.feh.skill.effect.SkillEffect
import me.kumatheta.feh.skill.effect.StaffAsNormalBasic
import me.kumatheta.feh.skill.effect.TriangleAdept
import me.kumatheta.feh.skill.effect.Vantage
import me.kumatheta.feh.skill.effect.cooldown.BreathEffect
import me.kumatheta.feh.skill.effect.cooldown.FlashingBlade3
import me.kumatheta.feh.skill.effect.cooldown.HeavyBlade3
import me.kumatheta.feh.skill.effect.followup.QuickRiposte
import me.kumatheta.feh.skill.effect.incombatstat.blow
import me.kumatheta.feh.skill.effect.incombatstat.bond
import me.kumatheta.feh.skill.effect.incombatstat.boost
import me.kumatheta.feh.skill.effect.incombatstat.rangeDefStat
import me.kumatheta.feh.skill.effect.incombatstat.solo
import me.kumatheta.feh.skill.effect.incombatstat.stance
import me.kumatheta.feh.skill.effect.postcombat.seal
import me.kumatheta.feh.skill.effect.startofturn.AirOrder3
import me.kumatheta.feh.skill.effect.startofturn.ArmorMarch3
import me.kumatheta.feh.skill.effect.startofturn.InfantryPulse
import me.kumatheta.feh.skill.effect.startofturn.Ploy
import me.kumatheta.feh.skill.effect.startofturn.QuickenedPulse
import me.kumatheta.feh.skill.effect.startofturn.Renewal
import me.kumatheta.feh.skill.effect.startofturn.SparklingBoost
import me.kumatheta.feh.skill.effect.startofturn.TimePulse3
import me.kumatheta.feh.skill.effect.startofturn.Upheaval
import me.kumatheta.feh.skill.effect.startofturn.chill
import me.kumatheta.feh.skill.effect.startofturn.hone
import me.kumatheta.feh.skill.effect.startofturn.opening
import me.kumatheta.feh.skill.effect.startofturn.resBasedPloy3
import me.kumatheta.feh.skill.effect.startofturn.sabotage
import me.kumatheta.feh.skill.effect.startofturn.tactics
import me.kumatheta.feh.skill.effect.startofturn.threaten
import me.kumatheta.feh.skill.effect.startofturn.wave
import me.kumatheta.feh.skill.effect.supportincombat.closeGuard
import me.kumatheta.feh.skill.effect.supportincombat.distantGuard
import me.kumatheta.feh.skill.effect.supportincombat.drive
import me.kumatheta.feh.skill.effect.supportincombat.spur
import me.kumatheta.feh.skill.effect.teleport.Aerobatics3
import me.kumatheta.feh.skill.effect.teleport.FlierFormation3
import me.kumatheta.feh.skill.effect.teleport.Guidance3
import me.kumatheta.feh.skill.passive.BoldFighter3
import me.kumatheta.feh.skill.passive.DefFeint3
import me.kumatheta.feh.skill.passive.DullClose3
import me.kumatheta.feh.skill.passive.DullRange3
import me.kumatheta.feh.skill.passive.Guard
import me.kumatheta.feh.skill.passive.LiveToServe3
import me.kumatheta.feh.skill.passive.MysticBoost3
import me.kumatheta.feh.skill.passive.ShieldPulse3
import me.kumatheta.feh.skill.passive.fury
import me.kumatheta.feh.skill.passive.poisonStrike
import me.kumatheta.feh.skill.passive.pulseSmoke3
import me.kumatheta.feh.skill.passive.smoke
import me.kumatheta.feh.skill.passive.waryFighter
import me.kumatheta.feh.skill.passive.windsweep
import me.kumatheta.feh.skill.passive.wrath

fun singleEffectSkills(): Sequence<Pair<String, SkillEffect>> = sequenceOf(
    "Upheaval" to Upheaval,
    "Close Counter" to CounterAnyRangeBasic,
    "Distant Counter" to CounterAnyRangeBasic,
    "Seal Atk/Def 2" to seal(atk = -5, def = -5),

    "Atk/Def Bond 1" to bond(atk = 3, def = 3),
    "Atk/Def Bond 2" to bond(atk = 4, def = 4),
    "Atk/Def Bond 3" to bond(atk = 5, def = 5),
    "Atk/Res Bond 1" to bond(atk = 3, res = 3),
    "Atk/Res Bond 2" to bond(atk = 4, res = 4),
    "Atk/Res Bond 3" to bond(atk = 5, res = 5),
    "Atk/Spd Bond 1" to bond(atk = 3, spd = 3),
    "Atk/Spd Bond 2" to bond(atk = 4, spd = 4),
    "Atk/Spd Bond 3" to bond(atk = 5, spd = 5),
    "Spd/Res Bond 1" to bond(spd = 3, res = 3),

    "Atk/Spd Solo 3" to solo(atk = 6, spd = 6),
    "Atk/Def Solo 3" to solo(atk = 6, def = 6),
    "Spd/Def Solo 3" to solo(spd = 6, def = 6),
    "Def/Res Solo 3" to solo(def = 6, res = 6),

    "Death Blow 3" to blow(atk = 6),
    "Darting Blow 3" to blow(spd = 6),
    "Armored Blow 3" to blow(def = 6),
    "Swift Sparrow 2" to blow(atk = 4, spd = 4),
    "Sturdy Blow 2" to blow(atk = 4, def = 4),
    "Mirror Strike 2" to blow(atk = 4, res = 4),
    "Swift Strike 2" to blow(spd = 4, def = 4),

    "Fierce Stance 3" to stance(atk = 6),
    "Steady Stance 3" to stance(def = 6),
    "Mirror Stance 2" to stance(atk = 4, res = 4),
    "Steady Posture 2" to stance(spd = 4, def = 4),
    "Bracing Stance 2" to stance(def = 4, res = 4),

    "Guidance 3" to Guidance3,

    "Spd Smoke 3" to smoke(spd = -7),
    "Def Smoke 3" to smoke(def = -7),
    "Res Smoke 3" to smoke(res = -7),

    "Spd Tactics 3" to tactics(spd = 6),
    "Def Tactics 3" to tactics(def = 6),
    "Atk Tactics 3" to tactics(atk = 6),

    "Panic Ploy 3" to Ploy { self, foe ->
        if (foe.currentHp < self.currentHp) {
            foe.addNegativeStatus(NegativeStatus.PANIC)
        }
    },
    "Res Ploy 3" to resBasedPloy3(res = -5),
    "Atk Ploy 3" to resBasedPloy3(atk = -5),

    "Quick Riposte 3" to QuickRiposte(70),
    "Quick Riposte 2" to QuickRiposte(80),
    "Quick Riposte 1" to QuickRiposte(90),

    "Vantage 3" to Vantage(75),
    "Desperation 3" to Desperation(75),
    "Armor March 3" to ArmorMarch3,

    "Sabotage Atk 3" to sabotage(atk = -7),
    "Distant Def 3" to rangeDefStat(def = 6, res = 6),

    "Hone Atk 3" to hone(atk = 4),
    "Fortify Res 4" to hone(res = 7),
    "Fortify Fliers" to hone(def = 6, res = 6, moveType = MoveType.FLYING),
    "Wrathful Staff 3" to StaffAsNormalBasic,
    "Hardy Bearing 3" to DisablePriorityChangeBasic,

    "Time's Pulse 3" to TimePulse3,
    "Quickened Pulse" to QuickenedPulse,

    "Lunge" to SwapEffect.toPostInitiateMovement(),
    "Hit and Run" to HitAndRunEffect.toPostInitiateMovement(),

    "Chill Def 3" to chill(def = -7) {
        it.visibleStat.def
    },
    "Chill Res 3" to chill(res = -7) {
        it.visibleStat.res
    },

    "Odd Atk Wave 3" to wave(atk = 6, oddTurn = true),
    "Odd Spd Wave 3" to wave(spd = 6, oddTurn = true),
    "Odd Def Wave 3" to wave(def = 6, oddTurn = true),
    "Odd Res Wave 3" to wave(res = 6, oddTurn = true),
    "Even Atk Wave 3" to wave(atk = 6, oddTurn = false),
    "Even Spd Wave 3" to wave(spd = 6, oddTurn = false),
    "Even Def Wave 3" to wave(def = 6, oddTurn = false),
    "Even Res Wave 3" to wave(res = 6, oddTurn = false),

    "Infantry Pulse 3" to InfantryPulse(minDiff = 1),
    "Threaten Def 3" to threaten(def = -5),

    "Pass 3" to Pass(percentageHp = 25),
    "Obstruct 3" to Obstruct(percentageHp = 25),

    "HP+5" to Stat(hp = 5).toExtraStat(),
    "Res+3" to Stat(res = 3).toExtraStat(),
    "Atk/Res 2" to Stat(atk = 2, res = 2).toExtraStat(),
    "Fortress Res 3" to Stat(atk = -3, res = 5).toExtraStat(),
    "Fortress Def 3" to Stat(atk = -3, def = 5).toExtraStat(),
    "Life and Death 3" to Stat(atk = 5, spd = 5, def = -5, res = -5).toExtraStat(),
    "B Duel Flying 3" to Stat(hp = 5).toExtraStat(),
    "Atk/Spd 2" to Stat(atk = 2, spd = 2).toExtraStat(),

    "Spur Atk/Spd 2" to spur(atk = 2, spd = 2),
    "Spur Def/Res 2" to spur(def = 3, res = 3),
    "Spur Def 3" to spur(def = 4),
    "Spur Res 3" to spur(res = 4),
    "Spur Spd 1" to spur(spd = 2),
    "Spur Spd 2" to spur(spd = 3),
    "Spur Spd 3" to spur(spd = 4),
    "Earth Boost 3" to boost(def = 6),
    "Wind Boost 3" to boost(spd = 6),
    "Fire Boost 3" to boost(atk = 6),
    "Distant Guard 3" to distantGuard(Stat(def = 4, res = 4)),
    "Close Guard 3" to closeGuard(Stat(def = 4, res = 4)),

    "Heavy Blade 3" to HeavyBlade3,
    "Flashing Blade 3" to FlashingBlade3,
    "Pulse Smoke 3" to pulseSmoke3,
    "Renewal 3" to Renewal(2),

    "Live to Serve 3" to LiveToServe3,
    "Null Follow-up 3" to NeutralizeFollowUpBasic,
    "Guard 3" to Guard(percentageHp = 80),
    "Triangle Adept 3" to TriangleAdept(20),
    "Dull Close 3" to DullClose3,
    "Dull Range 3" to DullRange3,
    "Iote's Shield" to NeutralizeEffectiveAgainstMovement(MoveType.FLYING),

    "Def Feint 3" to DefFeint3,
    "Sparkling Boost" to SparklingBoost,
    "Drive Atk 2" to drive(atk = 3),
    "Drive Spd 2" to drive(spd = 3),
    "Drive Def 2" to drive(def = 3),
    "Drive Res 2" to drive(res = 3),
    "Goad Cavalry" to drive(atk = 4, spd = 4, moveType = MoveType.CAVALRY),

    "Aerobatics 3" to Aerobatics3,
    "Flier Formation 3" to FlierFormation3,
    "Air Orders 3" to AirOrder3,
    "Atk Opening 3" to opening(atk = 6) {
        it.startOfTurnStat.atk
    }
)

fun mixedEffectSkills(): Sequence<Pair<String, Skill>> = sequenceOf(
    "Bold Fighter 3" to BoldFighter3,
    "Warding Breath" to sequenceOf(BreathEffect, stance(res = 4)).toSkill(),

    "Sword Breaker 3" to breaker(Sword, 50),
    "Bow Breaker 3" to breaker(BowC, 50),
    "B Tome breaker 3" to breaker(MagicB, 50),

    "Wary Fighter 3" to waryFighter(50),
    "Windsweep 3" to windsweep(1),
    "Wrath 3" to wrath(75),
    "Poison Strike 3" to poisonStrike(10),
    "Fury 3" to fury(3),
    "Mystic Boost 3" to MysticBoost3,
    "Shield Pulse 3" to ShieldPulse3
)

val ALL_PASSIVES = (singleEffectSkills().map { it.first to it.second.toSkill() } + mixedEffectSkills()).toSkillMap()
