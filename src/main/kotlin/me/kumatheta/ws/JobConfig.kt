package me.kumatheta.ws

import me.kumatheta.feh.BattleMap
import me.kumatheta.feh.BattleState
import me.kumatheta.feh.InternalBattleMap
import me.kumatheta.feh.UnitAction
import me.kumatheta.feh.mcts.FehBoardConfig
import me.kumatheta.feh.mcts.FehMove
import me.kumatheta.feh.mcts.calculateHeroBattleScore
import me.kumatheta.feh.mcts.dancerFirst
import me.kumatheta.feh.mcts.toRating
import me.kumatheta.feh.mcts.toScore
import me.kumatheta.feh.util.CacheBattleMap
import me.kumatheta.mcts.*
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.minutes

@ExperimentalTime
data class FehJobConfig<S : Score<FehMove>, M: ScoreManagerFactory<FehMove, S>>(
    val scoreManagerFactory: M,
    val mapName: String,
    val phaseLimit: Int = 20,
    val maxTurnBeforeEngage: Int = 3,
    val canRearrange: Boolean = true,
    val parallelCount: Int = 20,
    val toRating: UnitAction.(config: FehBoardConfig) -> Int = UnitAction::dancerFirst,
    val calculateScore: BattleState.(config: FehBoardConfig) -> Long = BattleState::toScore,
    val toInternalBattleMap: BattleMap.() -> InternalBattleMap = {
        CacheBattleMap(this)
    },
    val startingMoves: Sequence<FehMove>? = null,
    val moveDownCriteria: MoveDownCriteria = MoveDownCriteria(20.minutes, 1_000_000, 620000)
)

@ExperimentalTime
data class MoveDownCriteria(val maxDuration: Duration?, val maxTries: Int?, val maxNodes: Int?)

