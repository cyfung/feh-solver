package me.kumatheta.feh.skill

import me.kumatheta.feh.Beast
import me.kumatheta.feh.BowC
import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.MagicB
import me.kumatheta.feh.MoveType
import me.kumatheta.feh.NegativeStatus
import me.kumatheta.feh.Stat
import me.kumatheta.feh.StatType
import me.kumatheta.feh.get
import me.kumatheta.feh.skill.assist.movement.DrawBackEffect
import me.kumatheta.feh.skill.assist.movement.HitAndRunEffect
import me.kumatheta.feh.skill.assist.movement.SwapEffect
import me.kumatheta.feh.skill.effect.CounterAnyRangeBasic
import me.kumatheta.feh.skill.effect.Desperation
import me.kumatheta.feh.skill.effect.DisableFoeAllySupportEffectBasic
import me.kumatheta.feh.skill.effect.DisablePriorityChangeBasic
import me.kumatheta.feh.skill.effect.NeutralizeEffectiveAgainstMovement
import me.kumatheta.feh.skill.effect.NeutralizePenalty
import me.kumatheta.feh.skill.effect.Obstruct
import me.kumatheta.feh.skill.effect.Pass
import me.kumatheta.feh.skill.effect.SkillEffect
import me.kumatheta.feh.skill.effect.StaffAsNormalBasic
import me.kumatheta.feh.skill.effect.TriangleAdept
import me.kumatheta.feh.skill.effect.Vantage
import me.kumatheta.feh.skill.effect.cooldown.BreathEffect
import me.kumatheta.feh.skill.effect.cooldown.FlashingBlade3
import me.kumatheta.feh.skill.effect.cooldown.HeavyBlade3
import me.kumatheta.feh.skill.effect.followup.NullFollowUp3
import me.kumatheta.feh.skill.effect.followup.QuickRiposte
import me.kumatheta.feh.skill.effect.incombatstat.allForm
import me.kumatheta.feh.skill.effect.incombatstat.blow
import me.kumatheta.feh.skill.effect.incombatstat.bond
import me.kumatheta.feh.skill.effect.incombatstat.boost
import me.kumatheta.feh.skill.effect.incombatstat.brazen
import me.kumatheta.feh.skill.effect.incombatstat.closeDef
import me.kumatheta.feh.skill.effect.incombatstat.distantDef
import me.kumatheta.feh.skill.effect.incombatstat.solo
import me.kumatheta.feh.skill.effect.incombatstat.stance
import me.kumatheta.feh.skill.effect.postcombat.SavageBlow
import me.kumatheta.feh.skill.effect.postcombat.Seal
import me.kumatheta.feh.skill.effect.skillEffects
import me.kumatheta.feh.skill.effect.startofturn.Orders3
import me.kumatheta.feh.skill.effect.startofturn.ArmorMarch3
import me.kumatheta.feh.skill.effect.startofturn.InfantryPulse
import me.kumatheta.feh.skill.effect.startofturn.Opening
import me.kumatheta.feh.skill.effect.startofturn.Ploy
import me.kumatheta.feh.skill.effect.startofturn.PulseTie
import me.kumatheta.feh.skill.effect.startofturn.QuickenedPulse
import me.kumatheta.feh.skill.effect.startofturn.Renewal
import me.kumatheta.feh.skill.effect.startofturn.SparklingBoost
import me.kumatheta.feh.skill.effect.startofturn.TimePulse3
import me.kumatheta.feh.skill.effect.startofturn.Upheaval
import me.kumatheta.feh.skill.effect.startofturn.allThreaten
import me.kumatheta.feh.skill.effect.startofturn.chill
import me.kumatheta.feh.skill.effect.startofturn.hone
import me.kumatheta.feh.skill.effect.startofturn.honeWeaponType
import me.kumatheta.feh.skill.effect.startofturn.resBasedPloy3
import me.kumatheta.feh.skill.effect.startofturn.sabotage
import me.kumatheta.feh.skill.effect.startofturn.tactics
import me.kumatheta.feh.skill.effect.startofturn.wave
import me.kumatheta.feh.skill.effect.supportincombat.closeGuard
import me.kumatheta.feh.skill.effect.supportincombat.distantGuard
import me.kumatheta.feh.skill.effect.supportincombat.drive
import me.kumatheta.feh.skill.effect.supportincombat.spur
import me.kumatheta.feh.skill.effect.teleport.Aerobatics3
import me.kumatheta.feh.skill.effect.teleport.EscapeRoute
import me.kumatheta.feh.skill.effect.teleport.FlierFormation3
import me.kumatheta.feh.skill.effect.teleport.Guidance3
import me.kumatheta.feh.skill.effect.teleport.WingsOfMercy
import me.kumatheta.feh.skill.passive.BoldFighter3
import me.kumatheta.feh.skill.effect.assist.DefFeint3
import me.kumatheta.feh.skill.effect.assist.Link
import me.kumatheta.feh.skill.effect.startofturn.ArmoredBoots
import me.kumatheta.feh.skill.passive.DullClose3
import me.kumatheta.feh.skill.passive.DullRanged3
import me.kumatheta.feh.skill.passive.Guard
import me.kumatheta.feh.skill.passive.LiveToServe3
import me.kumatheta.feh.skill.passive.MysticBoost3
import me.kumatheta.feh.skill.passive.PanicSmoke3
import me.kumatheta.feh.skill.passive.PulseSmoke3
import me.kumatheta.feh.skill.passive.Repel3
import me.kumatheta.feh.skill.passive.ShieldPulse3
import me.kumatheta.feh.skill.passive.allLull
import me.kumatheta.feh.skill.passive.allPush4
import me.kumatheta.feh.skill.passive.fury
import me.kumatheta.feh.skill.passive.poisonStrike
import me.kumatheta.feh.skill.passive.statSmoke
import me.kumatheta.feh.skill.passive.waryFighter
import me.kumatheta.feh.skill.passive.windWaterSweep
import me.kumatheta.feh.skill.passive.wrath
import me.kumatheta.feh.statPairSequence
import me.kumatheta.feh.statSequence
import me.kumatheta.feh.toStat

val linkSkills
    get() = (1..3).asSequence().flatMap { level ->
        statPairSequence {
            val name = "${it.first}/${it.second} Link $level"
            name to Link(it.toStat(level * 2))
        }
    }

val sealSkills
    get() = (1..2).asSequence().flatMap { level ->
        statPairSequence {
            val name = "Seal ${it.first}/${it.second} $level"
            name to Seal(it.toStat(-(level * 2 + 1)))
        }
    } + (1..3).asSequence().flatMap { level ->
        statSequence {
            val name = "Seal $it $level"
            name to Seal(it.toStat(-(level * 2 + 1)))
        }
    }

val duelSkills = sequenceOf("R", "G", "B").flatMap {color ->
    sequenceOf("Flying", "Infantry").map {
        "$color Duel $it 3" to Stat(hp = 5).toExtraStat()
    }
}

val bondSkills
    get() = (1..3).asSequence().flatMap { level ->
        statPairSequence {
            val name = "${it.first}/${it.second} Bond $level"
            name to bond(it.toStat(2 + level))
        }
    }

val bond4Skills
    get() = statPairSequence {
        val name = "${it.first}/${it.second} Bond 4"
        val list = it.toList()
        name to skillEffects(
            bond(it.toStat(7)),
            object : NeutralizePenalty {
                override fun apply(combatStatus: CombatStatus<HeroUnit>): Sequence<StatType> {
                    return if (combatStatus.self.adjacentAllies(combatStatus.battleState).any()) {
                        list.asSequence()
                    } else {
                        emptySequence()
                    }
                }
            }
        ).toSkill()
    }

val soloSkills
    get() = (1..3).asSequence().flatMap { level ->
        statPairSequence {
            val name = "${it.first}/${it.second} Solo $level"
            name to solo(it.toStat(2 * level))
        }
    }

val savageBlowSkills
    get() = (1..3).asSequence().map { level ->
        "Savage Blow $level" to SavageBlow(1 + 2 * level)
    }

val dualStats
    get() = statPairSequence {
        val name = "${it.first}/${it.second} 2"
        name to it.toStat(2).toExtraStat()
    }

val singleStats
    get() = statSequence {
        val name = "$it +3"
        name to it.toStat(3).toExtraStat()
    }

val spurSkills
    get() = (1..2).asSequence().flatMap { level ->
        statPairSequence {
            val name = "Spur ${it.first}/${it.second} $level"
            name to spur(it.toStat(level + 1))
        }
    } + (1..3).asSequence().flatMap { level ->
        statSequence {
            val name = "Spur $it $level"
            name to spur(it.toStat(level + 1))
        }
    }



val baseBreakers
    get() = swordAxeLance.map {
        "${it.javaClass.simpleName} Breaker 3" to breaker(it, 50)
    }


fun allOpening() = (1..3).asSequence().flatMap { level ->
    statPairSequence {
        val name = "${it.first}/${it.second} Gap $level"
        name to Opening(it.toStat(-1 + 2 * level)) { unit ->
            unit.visibleStat[it.first] + unit.visibleStat[it.second]
        }
    } + statSequence {
        val name = "$it Opening $level"
        name to Opening(it.toStat(1 + 2 * level)) { unit ->
            unit.visibleStat[it]
        }
    }
}

fun singleEffectSkills(): Sequence<Pair<String, SkillEffect>> = linkSkills +
        sealSkills +
        duelSkills +
        bondSkills +
        soloSkills +
        allLull() +
        savageBlowSkills +
        allForm() +
        dualStats +
        singleStats +
        spurSkills +
        allThreaten() +
        allOpening() +
        sequenceOf(
            "Upheaval" to Upheaval,
            "Impenetrable Dark" to DisableFoeAllySupportEffectBasic,
            "Close Counter" to CounterAnyRangeBasic,
            "Distant Counter" to CounterAnyRangeBasic,

            "Death Blow 3" to blow(atk = 6),
            "Darting Blow 3" to blow(spd = 6),
            "Armored Blow 3" to blow(def = 6),
            "Warding Blow 3" to blow(res = 6),
            "Swift Sparrow 2" to blow(atk = 4, spd = 4),
            "Sturdy Blow 2" to blow(atk = 4, def = 4),
            "Mirror Strike 2" to blow(atk = 4, res = 4),
            "Steady Blow 2" to blow(spd = 4, def = 4),
            "Swift Strike 2" to blow(spd = 4, res = 4),

            "Fierce Stance 3" to stance(atk = 6),
            "Steady Stance 3" to stance(def = 6),
            "Warding Stance 3" to stance(res = 6),
            "Mirror Stance 2" to stance(atk = 4, res = 4),
            "Steady Posture 2" to stance(spd = 4, def = 4),
            "Bracing Stance 2" to stance(def = 4, res = 4),

            "Brazen Atk/Def 3" to brazen(atk = 7, def = 7),
            "Brazen Atk/Res 3" to brazen(atk = 7, res = 7),
            "Brazen Spd/Def 3" to brazen(spd = 7, def = 7),
            "Brazen Def/Res 3" to brazen(def = 7, res = 7),

            "Guidance 3" to Guidance3,

            "Atk Smoke 3" to statSmoke(atk = -7),
            "Spd Smoke 3" to statSmoke(spd = -7),
            "Def Smoke 3" to statSmoke(def = -7),
            "Res Smoke 3" to statSmoke(res = -7),

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
            "Armored Boots" to ArmoredBoots,

            "Yune's Whispers" to sabotage(atk = -6, spd = -6),
            "Sabotage Atk 3" to sabotage(atk = -7),
            "Distant Def 3" to distantDef(def = 6, res = 6),
            "Close Def 3" to closeDef(def = 6, res = 6),

            "Hone Atk 3" to hone(atk = 4),
            "Fortify Res 4" to hone(res = 7),
            "Fortify Fliers" to hone(def = 6, res = 6, moveType = MoveType.FLYING),
            "Fortify Beasts" to honeWeaponType<Beast>(def = 6, res = 6),
            "Wrathful Staff 3" to StaffAsNormalBasic,
            "Hardy Bearing 3" to DisablePriorityChangeBasic,

            "Time's Pulse 3" to TimePulse3,
            "Quickened Pulse" to QuickenedPulse,

            "Lunge" to SwapEffect.toPostInitiateMovement(),
            "Hit and Run" to HitAndRunEffect.toPostInitiateMovement(),
            "Drag Back" to DrawBackEffect.toPostInitiateMovement(),

            "Chill Atk 3" to chill(atk = -7) {
                it.visibleStat.atk
            },
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

            "Pass 3" to Pass(percentageHp = 25),
            "Obstruct 3" to Obstruct(percentageHp = 25),

            "HP +5" to Stat(hp = 5).toExtraStat(),
            "Fortress Res 3" to Stat(atk = -3, res = 5).toExtraStat(),
            "Fortress Def 3" to Stat(atk = -3, def = 5).toExtraStat(),
            "Fort. Def/Res 2" to Stat(atk = -3, def = 4, res = 4).toExtraStat(),
            "Life and Death 3" to Stat(atk = 5, spd = 5, def = -5, res = -5).toExtraStat(),
            "HP/Atk 2" to Stat(hp = 4, atk = 2).toExtraStat(),
            "Life and Death 3" to Stat(atk = 5, spd = 5, def = -5, res = -5).toExtraStat(),

            "Fire Boost 3" to boost(atk = 6),
            "Wind Boost 3" to boost(spd = 6),
            "Earth Boost 3" to boost(def = 6),
            "Water Boost 3" to boost(res = 6),
            "Distant Guard 3" to distantGuard(Stat(def = 4, res = 4)),
            "Close Guard 3" to closeGuard(Stat(def = 4, res = 4)),

            "Heavy Blade 3" to HeavyBlade3,
            "Flashing Blade 3" to FlashingBlade3,
            "Pulse Smoke 3" to PulseSmoke3,
            "Renewal 3" to Renewal(2),
            "Escape Route 3" to EscapeRoute(50),
            "Wings of Mercy 3" to WingsOfMercy(50),

            "Live to Serve 3" to LiveToServe3,
            "Guard 3" to Guard(percentageHp = 80),
            "Triangle Adept 3" to TriangleAdept(20),
            "Dull Close 3" to DullClose3,
            "Dull Ranged 3" to DullRanged3,
            "Iote's Shield" to NeutralizeEffectiveAgainstMovement(MoveType.FLYING),

            "Def Feint 3" to DefFeint3,
            "Sparkling Boost" to SparklingBoost,
            "Drive Atk 2" to drive(atk = 3),
            "Drive Spd 2" to drive(spd = 3),
            "Drive Def 2" to drive(def = 3),
            "Drive Res 2" to drive(res = 3),
            "Goad Cavalry" to drive(atk = 4, spd = 4, moveType = MoveType.CAVALRY),
            "Ward Cavalry" to drive(def = 4, res = 4, moveType = MoveType.CAVALRY),

            "Aerobatics 3" to Aerobatics3,
            "Flier Formation 3" to FlierFormation3,
            "Air Orders 3" to Orders3(isFlying = true),
            "Ground Orders 3" to Orders3(isFlying = true),
            "Even Pulse Tie 3" to PulseTie(1, evenNumberedTurn = true)
        )

fun mixedEffectSkills(): Sequence<Pair<String, Skill>> = allPush4() +
        bond4Skills +
        baseBreakers +
        sequenceOf(
    "Bold Fighter 3" to BoldFighter3,
    "Warding Breath" to sequenceOf(BreathEffect, stance(res = 4)).toSkill(),

    "Bow Breaker 3" to breaker(BowC, 50),
    "B Tomebreaker 3" to breaker(MagicB, 50),

    "Wary Fighter 3" to waryFighter(50),
    "Watersweep 3" to windWaterSweep(1, targetRes = true),
    "Windsweep 3" to windWaterSweep(1, targetRes = false),
    "Wrath 3" to wrath(75),
    "Poison Strike 3" to poisonStrike(10),
    "Fury 3" to fury(3),
    "Mystic Boost 3" to MysticBoost3,
    "Shield Pulse 3" to ShieldPulse3,
    "Null Follow-up 3" to NullFollowUp3,
    "Repel 3" to Repel3,
    "Panic Smoke 3" to PanicSmoke3
)

val ALL_PASSIVES = (singleEffectSkills().map { it.first to it.second.toSkill() } + mixedEffectSkills()).toSkillMap()
