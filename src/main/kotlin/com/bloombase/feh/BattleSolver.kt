package com.bloombase.feh

import java.lang.IllegalStateException
import java.util.*

private class PlayerMovement(
    val unitAction: UnitAction,
    val beforeState: BattleState,
    val moveIterator: Iterator<UnitAction>
)

class BattleSolver(private val battleMap: BattleMap, private val phraseLimit: Int) {

    fun solve(): BattleSolution {
        var lastPlayerMovement: PlayerMovement? = null
        val steps = LinkedList<PlayerMovement>()
        var battleState = BattleState(battleMap)

        mainLoop@ while (true) {
            while (true) {
                val nextMove = getNextMovement(lastPlayerMovement, battleState)
                if (nextMove == null) {
                    lastPlayerMovement = rollback(steps) ?: return NoSolution
                    battleState = lastPlayerMovement.beforeState
                    continue@mainLoop
                }
                val newState = battleState.copy()
                executeMove(newState, nextMove, steps)
                if (newState.phrase > phraseLimit) {
                    lastPlayerMovement = rollback(steps) ?: throw IllegalStateException()
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
        steps: LinkedList<PlayerMovement>
    ) {
        when (newState.playerMove(nextMove.unitAction)) {
            BattleState.MovementResult.PLAYER_WIN -> {
                println(steps)
                throw RuntimeException("win")
            }
            BattleState.MovementResult.PLAYER_UNIT_DIED -> throw RuntimeException("died")
            BattleState.MovementResult.PHRASE_CHANGE -> {
                steps.add(nextMove)
                newState.enemyMoves()
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
            val nextMove = lastPlayerMovement.moveIterator.nextOrNull()
            if (nextMove == null) {
                null
            } else {
                PlayerMovement(
                    nextMove,
                    battleState,
                    lastPlayerMovement.moveIterator
                )
            }
        } else {
            val moveIterator = battleState.getAllPlayerMovements().iterator()
            PlayerMovement(
                moveIterator.next(),
                beforeState = battleState,
                moveIterator = moveIterator
            )
        }
    }

    private fun rollback(steps: LinkedList<PlayerMovement>): PlayerMovement? {
        return if (steps.isNotEmpty()) {
            steps.removeLast()
        } else {
            null
        }
    }

    private fun <T> Iterator<T>.nextOrNull(): T? {
        return if (hasNext()) {
            next()
        } else {
            null
        }
    }

}

sealed class BattleSolution
object NoSolution : BattleSolution()