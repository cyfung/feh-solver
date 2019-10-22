package com.bloombase.feh

import java.util.*

class BattleSolver(private val battleMap: BattleMap) {

    var count = 0

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
                if (newState.phrase > 5) {
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
            BattleState.MovementResult.PLAYER_WIN -> {
                println(steps)
                throw RuntimeException("win")
            }
            BattleState.MovementResult.PLAYER_UNIT_DIED -> throw RuntimeException("died")
            BattleState.MovementResult.PHRASE_CHANGE -> {
                steps.add(nextMove)
                steps.add(TurnEnd)
                println("count ${count++}")
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
                        val moveTargets = battleState.moveTargets(nextAvailableUnit).map { it.position }.iterator()
                        val move = moveTargets.next()
                        val attackTargets = battleState.attackTargets(nextAvailableUnit, move).iterator()
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
                        battleState.attackTargets(lastPlayerMovement.heroUnit, nextMoveTarget).iterator()
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
            check(availableUnits.hasNext())
            val heroUnit = availableUnits.next()
            val moveTargets = battleState.moveTargets(heroUnit).map { it.position }.iterator()
            val move = moveTargets.next()
            val attackTargets = battleState.attackTargets(
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

    private fun availableUnits(battleState: BattleState): Sequence<HeroUnit> {
        return battleState.playerUnits.filter { it.available }
    }
}

sealed class BattleSolution
object NoSolution : BattleSolution()