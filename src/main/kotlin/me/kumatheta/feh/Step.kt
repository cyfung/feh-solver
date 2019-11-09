package me.kumatheta.feh

sealed class UnitAction(
    val heroUnitId: Int,
    val moveTarget: Position
)

class MoveOnly(
    heroUnitId: Int,
    moveTarget: Position
) : UnitAction(heroUnitId, moveTarget)

class MoveAndAttack (
    heroUnitId: Int,
    moveTarget: Position,
    val attackTargetId: Int
) : UnitAction(heroUnitId, moveTarget)

class MoveAndAssist(
    heroUnitId: Int,
    moveTarget: Position,
    val assistTargetId: Int
) : UnitAction(heroUnitId, moveTarget)