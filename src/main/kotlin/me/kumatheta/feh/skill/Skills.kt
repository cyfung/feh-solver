package me.kumatheta.feh.skill

import me.kumatheta.feh.BattleState
import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.Stat
import me.kumatheta.feh.skill.effect.ExtraStat
import me.kumatheta.feh.skill.effect.InCombatStatEffect
import me.kumatheta.feh.skill.effect.PostInitiateMovement
import me.kumatheta.feh.skill.effect.SkillEffect

fun aoeDebuffFoe(
    combatStatus: CombatStatus<InCombatStat>,
    stat: Stat
) {
    combatStatus.foe.heroUnit.nearbyAlliesAndSelf(combatStatus.battleState, 2)
        .forEach {
            it.cachedEffect.applyDebuff(stat)
        }
}

fun aoeBuffAlly(
    combatStatus: CombatStatus<InCombatStat>,
    stat: Stat
) {
    combatStatus.battleState.unitsSeq(combatStatus.self.heroUnit.team)
        .filter { it.position.distanceTo(combatStatus.self.heroUnit.position) <= 2 }
        .forEach {
            it.cachedEffect.applyBuff(stat)
        }
}

fun HeroUnit.adjacentAllies(
    battleState: BattleState
) = allies(battleState).filter { it.position.distanceTo(position) == 1 }

fun HeroUnit.adjacentAlliesAndSelf(
    battleState: BattleState
) = allies(battleState).filter { it.position.distanceTo(position) == 1 } + this

fun HeroUnit.nearbyAllies(
    battleState: BattleState,
    maxRange: Int
): Sequence<HeroUnit> {
    return allies(battleState).filter {
        it.position.distanceTo(position) <= maxRange
    }
}

fun HeroUnit.nearbyAlliesAndSelf(
    battleState: BattleState,
    maxRange: Int
): Sequence<HeroUnit> {
    return alliesAndSelf(battleState).filter {
        it.position.distanceTo(position) <= maxRange
    }
}

fun HeroUnit.allies(battleState: BattleState) =
    battleState.unitsSeq(team).filterNot { it == this }

fun HeroUnit.alliesAndSelf(battleState: BattleState) =
    battleState.unitsSeq(team)

fun HeroUnit.inCardinalDirection(target: HeroUnit) =
    target.position.x == position.x || target.position.y == position.y

fun MovementEffect.toPostInitiateMovement() = PostInitiateMovement(this)

fun SkillEffect.toSkill(): Skill {
    return object : Skill {
        override val effects: List<SkillEffect> = listOf(this@toSkill)
    }
}

fun Sequence<SkillEffect>.toSkill(): Skill {
    return object : Skill {
        override val effects: List<SkillEffect> = this@toSkill.toList()
    }
}


fun Stat.toExtraStat(): ExtraStat {
    return ExtraStat(this)
}

fun Stat.toInCombatStatEffect(): InCombatStatEffect {
    return object : InCombatStatEffect {
        override fun apply(combatStatus: CombatStatus<HeroUnit>): Stat {
            return this@toInCombatStatEffect
        }
    }
}