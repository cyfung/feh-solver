package me.kumatheta.feh.test

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import me.kumatheta.feh.BattleState
import me.kumatheta.feh.mcts.FehMove
import me.kumatheta.feh.mcts.calculateScoreV1
import me.kumatheta.feh.util.NoCacheBattleMap
import me.kumatheta.mcts.UCT
import me.kumatheta.ws.FehJobConfig
import me.kumatheta.ws.startNewJob
import me.kumatheta.ws.toJobInfo
import kotlin.time.ExperimentalTime

@ExperimentalCoroutinesApi
@ExperimentalTime
fun main() {
    val jobConfig = FehJobConfig(
        scoreManager = UCT<FehMove>(),
        mapName = "bhb titania mist",
        phaseLimit = 20,
        maxTurnBeforeEngage = 20,
        parallelCount = 1,
        canRearrange = false,
        toRating = { 1 },
        calculateScore = BattleState::calculateScoreV1,
        toInternalBattleMap = {
            NoCacheBattleMap(this)
        }
    )
    runBlocking {
        val jobInfo = jobConfig.toJobInfo()
        jobInfo.startNewJob()
        jobInfo.completableJob.join()
    }
}


