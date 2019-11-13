package me.kumatheta.feh

sealed class UnitAction(
    val heroUnitId: Int,
    val moveTarget: Position
) {
    override fun toString(): String {
        return "UnitAction(heroUnitId=$heroUnitId, moveTarget=$moveTarget)"
    }
}

class MoveOnly(
    heroUnitId: Int,
    moveTarget: Position
) : UnitAction(heroUnitId, moveTarget) {
    override fun toString(): String {
        return "MoveOnly() ${super.toString()}"
    }
}

class MoveAndAttack (
    heroUnitId: Int,
    moveTarget: Position,
    val attackTargetId: Int
) : UnitAction(heroUnitId, moveTarget) {
    override fun toString(): String {
        return "MoveAndAttack(attackTargetId=$attackTargetId) ${super.toString()}"
    }
}

class MoveAndAssist(
    heroUnitId: Int,
    moveTarget: Position,
    val assistTargetId: Int
) : UnitAction(heroUnitId, moveTarget) {
    override fun toString(): String {
        return "MoveAndAssist(assistTargetId=$assistTargetId) ${super.toString()}"
    }
}