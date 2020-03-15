package me.kumatheta.feh.test

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import me.kumatheta.feh.BattleState
import me.kumatheta.feh.UnitAction
import me.kumatheta.feh.mcts.FehMove
import me.kumatheta.feh.mcts.calculateScoreV1
import me.kumatheta.feh.mcts.toNewScore
import me.kumatheta.feh.mcts.toRating
import me.kumatheta.feh.util.NoCacheBattleMap
import me.kumatheta.mcts.UCT
import me.kumatheta.mcts.hybridDynamicUCTTune
import me.kumatheta.ws.FehJobConfig
import me.kumatheta.ws.startNewJob
import me.kumatheta.ws.toJobInfo
import kotlin.time.ExperimentalTime

@ExperimentalCoroutinesApi
@ExperimentalTime
fun main() {
    val jobConfig = FehJobConfig(
        scoreManagerFactory = hybridDynamicUCTTune(),
        mapName = "grandmaster 51",
        phaseLimit = 7,
        maxTurnBeforeEngage = 3,
        parallelCount = 20,
        canRearrange = false,
        toRating = UnitAction::toRating,
        calculateScore = BattleState::toNewScore
    )
    runBlocking {
        val jobInfo = jobConfig.toJobInfo()
        jobInfo.startNewJob()
        jobInfo.completableJob.join()
    }
}


