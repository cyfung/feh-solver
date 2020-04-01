package me.kumatheta.feh.test

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import me.kumatheta.feh.MoveAndAssist
import me.kumatheta.feh.MoveAndAttack
import me.kumatheta.feh.MoveOnly
import me.kumatheta.feh.mcts.FehMove
import me.kumatheta.feh.mcts.NormalMove
import me.kumatheta.feh.mcts.Rearrange
import me.kumatheta.mcts.hybridDynamicUCTTune
import me.kumatheta.ws.FehJobConfig
import me.kumatheta.ws.startNewJob
import me.kumatheta.ws.toJobInfo
import kotlin.time.ExperimentalTime

@ExperimentalCoroutinesApi
@ExperimentalTime
fun main() {
    val jobConfig = FehJobConfig(
        scoreManagerFactory = hybridDynamicUCTTune<FehMove>(),
//        scoreManagerFactory = DynamicUCTTuned<FehMove>().toFactory(),
        mapName = "bramimond infernal",
        startingMoves = sequenceOf(
            Rearrange(listOf(3, 2, 4, 1)),
            NormalMove(MoveAndAttack(heroUnitId = 1, moveTargetX = 2, moveTargetY = 1, attackTargetId = 9)),
            NormalMove(MoveAndAttack(heroUnitId = 3, moveTargetX = 3, moveTargetY = 2, attackTargetId = 9)),
            NormalMove(MoveOnly(heroUnitId = 2, moveTargetX = 5, moveTargetY = 3)),
            NormalMove(MoveAndAssist(heroUnitId = 4, moveTargetX = 3, moveTargetY = 1, assistTargetId = 1)),
            NormalMove(MoveAndAttack(heroUnitId = 1, moveTargetX = 4, moveTargetY = 1, attackTargetId = 10)),
            NormalMove(MoveAndAttack(heroUnitId = 3, moveTargetX = 3, moveTargetY = 0, attackTargetId = 13)),
            NormalMove(MoveOnly(heroUnitId = 4, moveTargetX = 3, moveTargetY = 1)),
            NormalMove(MoveOnly(heroUnitId = 2, moveTargetX = 5, moveTargetY = 5)),
            NormalMove(MoveAndAttack(heroUnitId = 1, moveTargetX = 4, moveTargetY = 1, attackTargetId = 13)),
            NormalMove(MoveAndAttack(heroUnitId = 1, moveTargetX = 5, moveTargetY = 2, attackTargetId = 16)),
            NormalMove(MoveAndAttack(heroUnitId = 3, moveTargetX = 3, moveTargetY = 0, attackTargetId = 8)),
            NormalMove(MoveAndAssist(heroUnitId = 4, moveTargetX = 5, moveTargetY = 1, assistTargetId = 1)),
            NormalMove(MoveAndAttack(heroUnitId = 1, moveTargetX = 5, moveTargetY = 4, attackTargetId = 14)),
            NormalMove(MoveAndAttack(heroUnitId = 2, moveTargetX = 5, moveTargetY = 6, attackTargetId = 12)),
            NormalMove(MoveAndAttack(heroUnitId = 1, moveTargetX = 4, moveTargetY = 5, attackTargetId = 17)),
            NormalMove(MoveAndAttack(heroUnitId = 3, moveTargetX = 3, moveTargetY = 1, attackTargetId = 18)),
            NormalMove(MoveAndAssist(heroUnitId = 4, moveTargetX = 3, moveTargetY = 0, assistTargetId = 3)),
            NormalMove(MoveAndAttack(heroUnitId = 3, moveTargetX = 3, moveTargetY = 1, attackTargetId = 5))
        )
    )
    runBlocking {
        val jobInfo = jobConfig.toJobInfo()
        jobInfo.startNewJob()
        jobInfo.completableJob.join()
    }
}


