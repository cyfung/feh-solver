package me.kumatheta.feh

sealed class UnitAction(
    val heroUnitId: Int,
    val moveTarget: Position
) {
    override fun toString(): String {
        return "UnitAction(heroUnitId=$heroUnitId, moveTarget=$moveTarget)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as UnitAction

        if (heroUnitId != other.heroUnitId) return false
        if (moveTarget != other.moveTarget) return false

        return true
    }

    override fun hashCode(): Int {
        var result = heroUnitId
        result = 31 * result + moveTarget.hashCode()
        return result
    }


}

class MoveOnly(
    heroUnitId: Int,
    moveTarget: Position
) : UnitAction(heroUnitId, moveTarget) {
    override fun toString(): String {
        return "MoveOnly() ${super.toString()}"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false
        return true
    }

}

class MoveAndAttack(
    heroUnitId: Int,
    moveTarget: Position,
    val attackTargetId: Int
) : UnitAction(heroUnitId, moveTarget) {
    override fun toString(): String {
        return "MoveAndAttack(attackTargetId=$attackTargetId) ${super.toString()}"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as MoveAndAttack

        if (attackTargetId != other.attackTargetId) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + attackTargetId
        return result
    }

}

class MoveAndBreak(
    heroUnitId: Int,
    moveTarget: Position,
    val obstacle: Position
) : UnitAction(heroUnitId, moveTarget) {
    override fun toString(): String {
        return "MoveAndBreak(obstacle=$obstacle) ${super.toString()}"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as MoveAndBreak

        if (obstacle != other.obstacle) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + obstacle.hashCode()
        return result
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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as MoveAndAssist

        if (assistTargetId != other.assistTargetId) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + assistTargetId
        return result
    }

}