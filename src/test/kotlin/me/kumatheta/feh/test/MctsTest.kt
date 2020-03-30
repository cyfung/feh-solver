package me.kumatheta.feh.test

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import me.kumatheta.feh.MoveAndAssist
import me.kumatheta.feh.MoveAndAttack
import me.kumatheta.feh.MoveAndBreak
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
        mapName = "death knight infernal",
        startingMoves = sequenceOf(
            Rearrange(listOf(1, 2, 4, 3)),
            NormalMove(MoveOnly(heroUnitId = 2, moveTargetX = 0, moveTargetY = 6)),
            NormalMove(MoveOnly(heroUnitId = 3, moveTargetX = 4, moveTargetY = 6)),
            NormalMove(MoveOnly(heroUnitId = 1, moveTargetX = 1, moveTargetY = 6)),
            NormalMove(MoveAndAssist(heroUnitId = 4, moveTargetX = 4, moveTargetY = 5, assistTargetId = 3)),
            NormalMove(MoveOnly(heroUnitId = 3, moveTargetX = 4, moveTargetY = 4)),
            NormalMove(MoveAndAttack(heroUnitId = 1, moveTargetX = 2, moveTargetY = 6, attackTargetId = 5)),
            NormalMove(MoveAndAssist(heroUnitId = 4, moveTargetX = 3, moveTargetY = 6, assistTargetId = 1)),
            NormalMove(MoveAndAttack(heroUnitId = 3, moveTargetX = 3, moveTargetY = 3, attackTargetId = 6)),
            NormalMove(MoveAndAttack(heroUnitId = 1, moveTargetX = 2, moveTargetY = 6, attackTargetId = 5)),
            NormalMove(MoveOnly(heroUnitId = 2, moveTargetX = 0, moveTargetY = 5)),
            NormalMove(MoveAndAttack(heroUnitId = 1, moveTargetX = 2, moveTargetY = 6, attackTargetId = 9)),
            NormalMove(MoveAndAssist(heroUnitId = 4, moveTargetX = 1, moveTargetY = 6, assistTargetId = 1)),
            NormalMove(MoveAndAttack(heroUnitId = 1, moveTargetX = 2, moveTargetY = 4, attackTargetId = 7)),
            NormalMove(MoveOnly(heroUnitId = 2, moveTargetX = 0, moveTargetY = 7)),
            NormalMove(MoveOnly(heroUnitId = 3, moveTargetX = 3, moveTargetY = 2)),
            NormalMove(MoveAndAttack(heroUnitId = 1, moveTargetX = 3, moveTargetY = 3, attackTargetId = 8)),
            NormalMove(MoveAndAttack(heroUnitId = 3, moveTargetX = 3, moveTargetY = 4, attackTargetId = 8))
        )
    )
    runBlocking {
        val jobInfo = jobConfig.toJobInfo()
        jobInfo.mcts.playOut(
            listOf(
                Rearrange(listOf(2, 1, 3, 4)),
                NormalMove(MoveOnly(heroUnitId = 4, moveTargetX = 5, moveTargetY = 1)),
                NormalMove(MoveOnly(heroUnitId = 1, moveTargetX = 2, moveTargetY = 1)),
                NormalMove(
                    MoveAndBreak(
                        heroUnitId = 3,
                        moveTargetX = 4,
                        moveTargetY = 0,
                        obstacleX = 3,
                        obstacleY = 1
                    )
                ),
                NormalMove(MoveOnly(heroUnitId = 2, moveTargetX = 1, moveTargetY = 2)),
                NormalMove(MoveAndAttack(heroUnitId = 3, moveTargetX = 4, moveTargetY = 1, attackTargetId = 10)),
                NormalMove(MoveAndAssist(heroUnitId = 4, moveTargetX = 4, moveTargetY = 0, assistTargetId = 3)),
                NormalMove(MoveAndAttack(heroUnitId = 3, moveTargetX = 4, moveTargetY = 3, attackTargetId = 8)),
                NormalMove(MoveAndAttack(heroUnitId = 1, moveTargetX = 2, moveTargetY = 2, attackTargetId = 10)),
                NormalMove(MoveOnly(heroUnitId = 2, moveTargetX = 1, moveTargetY = 1)),
                NormalMove(MoveAndAttack(heroUnitId = 3, moveTargetX = 4, moveTargetY = 3, attackTargetId = 16)),
                NormalMove(MoveAndAssist(heroUnitId = 4, moveTargetX = 4, moveTargetY = 2, assistTargetId = 3)),
                NormalMove(MoveAndAttack(heroUnitId = 1, moveTargetX = 1, moveTargetY = 2, attackTargetId = 6)),
                NormalMove(MoveAndAttack(heroUnitId = 3, moveTargetX = 4, moveTargetY = 3, attackTargetId = 9)),
                NormalMove(MoveOnly(heroUnitId = 2, moveTargetX = 1, moveTargetY = 1)),
                NormalMove(MoveAndAttack(heroUnitId = 3, moveTargetX = 3, moveTargetY = 3, attackTargetId = 16)),
                NormalMove(MoveAndAssist(heroUnitId = 4, moveTargetX = 4, moveTargetY = 3, assistTargetId = 3)),
                NormalMove(MoveAndAttack(heroUnitId = 3, moveTargetX = 1, moveTargetY = 3, attackTargetId = 14)),
                NormalMove(MoveOnly(heroUnitId = 2, moveTargetX = 1, moveTargetY = 1)),
                NormalMove(
                    MoveAndBreak(heroUnitId = 1, moveTargetX = 2, moveTargetY = 1, obstacleX = 3, obstacleY = 1)
                )
            )
        )
        jobInfo.startNewJob()
        jobInfo.completableJob.join()
    }
}


