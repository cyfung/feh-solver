package com.bloombase.feh

import java.util.*

class BattleSolver(private val battleMap: BattleMap) {

    fun solve(): BattleSolution {
        var lastPlayerMovement: PlayerMovement? = null
        val steps = LinkedList<Step>()
        var battleState = BattleState(battleMap)

        mainLoop@ while (true) {
            while (true) {
                val nextMove = getNextMovement(lastPlayerMovement, battleState)
                if (nextMove == null) {
                    lastPlayerMovement = rollback(steps) ?: return NoSolution
                    battleState = lastPlayerMovement.state
                    continue@mainLoop
                }
                val newState = battleState.copy()
                executeMove(newState, nextMove, steps)
                if (newState.phrase > 0) {
                    lastPlayerMovement = nextMove
                    continue@mainLoop
                }
                battleState = newState
                lastPlayerMovement = null
            }

        }
    }

    private fun executeMove(
        newState: BattleState,
        nextMove: PlayerMovement,
        steps: LinkedList<Step>
    ) {
        when (newState.playerMove(nextMove)) {
            BattleState.MovementResult.WIN -> {
                println(steps)
                throw RuntimeException("win")
            }
            BattleState.MovementResult.PLAYER_UNIT_DIED -> throw RuntimeException("died")
            BattleState.MovementResult.PHRASE_CHANGE -> {
                steps.add(nextMove)
                steps.add(TurnEnd)
                steps.addAll(newState.enemyMoves())
                steps.add(TurnEnd)
            }
            BattleState.MovementResult.NOTHING -> {
                steps.add(nextMove)
            }
        }
    }

    private fun getNextMovement(
        lastPlayerMovement: PlayerMovement?,
        battleState: BattleState
    ): PlayerMovement? {
        return if (lastPlayerMovement != null) {
            if (lastPlayerMovement.attackTargetId == null) {
                val nextMoveTarget = lastPlayerMovement.moveTargets.nextOrNull()
                if (nextMoveTarget == null) {
                    val nextAvailableUnit = lastPlayerMovement.availableUnits.nextOrNull()
                    if (nextAvailableUnit == null) {
                        null
                    } else {
                        val moveTargets = moveTargets(battleState, nextAvailableUnit).iterator()
                        val move = moveTargets.next()
                        val attackTargets = attackTargets(battleState, nextAvailableUnit, move).iterator()
                        val attack = attackTargets.nextOrNull()
                        lastPlayerMovement.copy(
                            heroUnit = nextAvailableUnit,
                            move = move,
                            attack = attack,
                            assist = null,
                            moveTargets = moveTargets,
                            attackTargets = attackTargets
                        )
                    }
                } else {
                    val attackTargets =
                        attackTargets(battleState, lastPlayerMovement.heroUnit, nextMoveTarget).iterator()
                    val attack = attackTargets.nextOrNull()
                    lastPlayerMovement.copy(
                        move = nextMoveTarget,
                        attack = attack,
                        assist = null,
                        attackTargets = attackTargets
                    )
                }
            } else {
                lastPlayerMovement.copy(
                    attack = lastPlayerMovement.attackTargets.nextOrNull(),
                    assist = null
                )
            }
        } else {
            val availableUnits = availableUnits(battleState).iterator()
            if (!availableUnits.hasNext()) {
                throw IllegalStateException()
            }
            val heroUnit = availableUnits.next()
            val moveTargets = moveTargets(battleState, heroUnit).iterator()
            val move = moveTargets.next()
            val attackTargets = attackTargets(
                battleState,
                heroUnit,
                move
            ).iterator()
            val attack = attackTargets.nextOrNull()
            PlayerMovement(
                heroUnit = heroUnit,
                move = move,
                attack = attack,
                assist = null,
                availableUnits = availableUnits,
                moveTargets = moveTargets,
                attackTargets = attackTargets,
                state = battleState
            )
        }
    }

    private fun rollback(steps: LinkedList<Step>): PlayerMovement? {
        while (steps.isNotEmpty()) {
            val last = steps.removeLast()
            if (last is PlayerMovement) {
                return last
            }
        }
        return null
    }

    private fun <T> Iterator<T>.nextOrNull(): T? {
        return if (hasNext()) {
            next()
        } else {
            null
        }
    }

    private fun attackTargets(
        battleState: BattleState,
        heroUnit: HeroUnit,
        position: Position
    ): Sequence<HeroUnit> {
        if (heroUnit.isEmptyHanded) {
            return emptySequence()
        }
        val range = heroUnit.weaponType.range

        return battleState.unitsAndPos(Team.ENEMY).filter { it.value.distanceTo(position) == range }.map { it.key }
    }

    private fun moveTargets(battleState: BattleState, heroUnit: HeroUnit): Sequence<Position> {
        val pos = battleState.reverseMap[heroUnit] ?: throw IllegalStateException()
        return generateSequence(Position(0, 0)) {
            nextPosition(it)
        }.filter {
            it == pos || battleState.forwardMap[it] == null
        }.filter {
            it.distanceTo(pos) <= heroUnit.travelDistance
        }
    }

    private fun nextPosition(position: Position?): Position? {
        if (position == null) {
            return Position(0, 0)
        }
        val newX = position.x + 1
        return if (newX == battleMap.size.x) {
            val newY = position.y + 1
            if (newY == battleMap.size.y) {
                null
            } else {
                Position(0, newY)
            }
        } else {
            Position(newX, position.y)
        }
    }

    private fun availableUnits(battleState: BattleState): Sequence<HeroUnit> {
        return battleState.playerUnits.filter { it.available }
    }
}

sealed class BattleSolution
object NoSolution : BattleSolution()

private fun <T> Sequence<T>.nextOrNull(predicate: (T) -> Boolean): T? {
    var takeNext = false
    return firstOrNull {
        if (takeNext) {
            return@firstOrNull true
        } else if (predicate(it)) {
            takeNext = true
        }
        return@firstOrNull false
    }
}