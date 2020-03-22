package me.kumatheta.feh.test

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import me.kumatheta.feh.BattleState
import me.kumatheta.feh.UnitAction
import me.kumatheta.feh.mcts.*
import me.kumatheta.feh.util.NoCacheBattleMap
import me.kumatheta.mcts.UCT
import me.kumatheta.mcts.VaryingUCT
import me.kumatheta.mcts.hybridDynamicUCTTune
import me.kumatheta.mcts.toFactory
import me.kumatheta.ws.FehJobConfig
import me.kumatheta.ws.startNewJob
import me.kumatheta.ws.toJobInfo
import kotlin.time.ExperimentalTime

@ExperimentalCoroutinesApi
@ExperimentalTime
fun main() {
    val jobConfig = FehJobConfig(
        scoreManagerFactory = hybridDynamicUCTTune<FehMove>(),
        mapName = "sothis infernal",
        phaseLimit = 20,
        maxTurnBeforeEngage = 3,
        parallelCount = 20,
        canRearrange = false,
        toRating = UnitAction::toRating,
        calculateScore = BattleState::toScore
    )
    runBlocking {
        val jobInfo = jobConfig.toJobInfo()
        jobInfo.startNewJob()
        jobInfo.completableJob.join()
    }
}


