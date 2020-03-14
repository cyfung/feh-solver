package me.kumatheta.ws

import me.kumatheta.feh.BattleMap
import me.kumatheta.feh.BattleState
import me.kumatheta.feh.InternalBattleMap
import me.kumatheta.feh.UnitAction
import me.kumatheta.feh.mcts.FehBoardConfig
import me.kumatheta.feh.mcts.FehMove
import me.kumatheta.feh.mcts.calculateHeroBattleScore
import me.kumatheta.feh.mcts.toRating
import me.kumatheta.feh.util.CacheBattleMap
import me.kumatheta.mcts.*

data class FehJobConfig<S : Score<FehMove>, M: ScoreManager<FehMove, S>>(
    val scoreManager: M,
    val mapName: String,
    val phaseLimit: Int = 20,
    val maxTurnBeforeEngage: Int = 3,
    val canRearrange: Boolean = true,
    val parallelCount: Int = 20,
    val toRating: UnitAction.(config: FehBoardConfig) -> Int = UnitAction::toRating,
    val calculateScore: BattleState.(phaseLimit: Int) -> Long = BattleState::calculateHeroBattleScore,
    val toInternalBattleMap: BattleMap.() -> InternalBattleMap = {
        CacheBattleMap(this)
    }
)