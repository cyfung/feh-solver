package com.bloombase.feh

sealed class Step
object TurnEnd : Step()
sealed class UnitMovement(
    val heroUnitId: Int,
    val move: Position,
    val attackTargetId: Int?,
    val assistTargetId: Int?
) : Step()

class EnemyMovement(
    heroUnit: Int,
    move: Position,
    attack: Int?,
    assist: Int?
) : UnitMovement(heroUnit, move, attack, assist)

class PlayerMovement(
    val heroUnit: HeroUnit,
    move: Position,
    val attack: HeroUnit?,
    val assist: HeroUnit?,
    val availableUnits: Iterator<HeroUnit>,
    val moveTargets: Iterator<Position>,
    val attackTargets: Iterator<HeroUnit>,
    val state: BattleState
) : UnitMovement(heroUnit.id, move, attack?.id, assist?.id) {
    fun copy(
        heroUnit: HeroUnit = this.heroUnit,
        move: Position = this.move,
        attack: HeroUnit? = this.attack,
        assist: HeroUnit? = this.assist,
        availableUnits: Iterator<HeroUnit> = this.availableUnits,
        moveTargets: Iterator<Position> = this.moveTargets,
        attackTargets: Iterator<HeroUnit> = this.attackTargets,
        state: BattleState = this.state
    ): PlayerMovement {
        return PlayerMovement(heroUnit, move, attack, assist, availableUnits, moveTargets, attackTargets, state)
    }
}
