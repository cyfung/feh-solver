package com.bloombase.feh

sealed class Step
object TurnEnd : Step()
class UnitMovement(
    val heroUnitId: Int,
    val move: Position,
    val attackTargetId: Int?,
    val assistTargetId: Int?
) : Step()
