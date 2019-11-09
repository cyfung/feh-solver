package me.kumatheta.feh

import java.lang.IllegalStateException
import java.util.*

private class PlayerMovement(
    val unitAction: me.kumatheta.feh.UnitAction,
    val beforeState: me.kumatheta.feh.BattleState,
    val moveIterator: Iterator<me.kumatheta.feh.UnitAction>
)

class BattleSolver(private val battleMap: me.kumatheta.feh.BattleMap, private val phraseLimit: Int) {

    fun solve(): me.kumatheta.feh.BattleSolution {
        var lastPlayerMovement: me.kumatheta.feh.PlayerMovement? = null
        val steps = LinkedList<me.kumatheta.feh.PlayerMovement>()
        var battleState = me.kumatheta.feh.BattleState(battleMap)

        mainLoop@ while (true) {
            while (true) {
                val nextMove = getNextMovement(lastPlayerMovement, battleState)
                if (nextMove == null) {
                    lastPlayerMovement = rollback(steps) ?: return me.kumatheta.feh.NoSolution
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
        newState: me.kumatheta.feh.BattleState,
        nextMove: me.kumatheta.feh.PlayerMovement,
        steps: LinkedList<me.kumatheta.feh.PlayerMovement>
    ) {
        when (newState.playerMove(nextMove.unitAction)) {
            me.kumatheta.feh.BattleState.MovementResult.PLAYER_WIN -> {
                println(steps)
                throw RuntimeException("win")
            }
            me.kumatheta.feh.BattleState.MovementResult.PLAYER_UNIT_DIED -> throw RuntimeException("died")
            me.kumatheta.feh.BattleState.MovementResult.PHRASE_CHANGE -> {
                steps.add(nextMove)
                newState.enemyMoves()
            }
            me.kumatheta.feh.BattleState.MovementResult.NOTHING -> {
                steps.add(nextMove)
            }
        }
    }

    private fun getNextMovement(
        lastPlayerMovement: me.kumatheta.feh.PlayerMovement?,
        battleState: me.kumatheta.feh.BattleState
    ): me.kumatheta.feh.PlayerMovement? {
        return if (lastPlayerMovement != null) {
            val nextMove = lastPlayerMovement.moveIterator.nextOrNull()
            if (nextMove == null) {
                null
            } else {
                me.kumatheta.feh.PlayerMovement(
                    nextMove,
                    battleState,
                    lastPlayerMovement.moveIterator
                )
            }
        } else {
            val moveIterator = battleState.getAllPlayerMovements().iterator()
            me.kumatheta.feh.PlayerMovement(
                moveIterator.next(),
                beforeState = battleState,
                moveIterator = moveIterator
            )
        }
    }

    private fun rollback(steps: LinkedList<me.kumatheta.feh.PlayerMovement>): me.kumatheta.feh.PlayerMovement? {
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
object NoSolution : me.kumatheta.feh.BattleSolution()