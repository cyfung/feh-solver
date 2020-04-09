package me.kumatheta.feh.test

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import me.kumatheta.feh.MoveAndAssist
import me.kumatheta.feh.MoveAndAttack
import me.kumatheta.feh.UnitAction
import me.kumatheta.feh.mcts.FehMove
import me.kumatheta.feh.mcts.NormalMove
import me.kumatheta.feh.mcts.toRating
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
        mapName = "grandmaster 54",
        toRating = UnitAction::toRating,
        startingMoves = sequenceOf(
            NormalMove(MoveAndAttack(heroUnitId = 2, moveTargetX = 2, moveTargetY = 5, attackTargetId = 8)),
            NormalMove(MoveAndAttack(heroUnitId = 4, moveTargetX = 2, moveTargetY = 3, attackTargetId = 9)),
            NormalMove(MoveAndAssist(heroUnitId = 1, moveTargetX = 2, moveTargetY = 4, assistTargetId = 2)),
            NormalMove(MoveAndAttack(heroUnitId = 3, moveTargetX = 2, moveTargetY = 2, attackTargetId = 9)),
            NormalMove(MoveAndAttack(heroUnitId = 4, moveTargetX = 1, moveTargetY = 3, attackTargetId = 7)),
            NormalMove(MoveAndAssist(heroUnitId = 3, moveTargetX = 2, moveTargetY = 3, assistTargetId = 2)),
            NormalMove(MoveAndAttack(heroUnitId = 1, moveTargetX = 0, moveTargetY = 5, attackTargetId = 12)),
            NormalMove(MoveAndAttack(heroUnitId = 2, moveTargetX = 1, moveTargetY = 5, attackTargetId = 13)),
            NormalMove(MoveAndAttack(heroUnitId = 4, moveTargetX = 3, moveTargetY = 3, attackTargetId = 10)),
            NormalMove(MoveAndAttack(heroUnitId = 2, moveTargetX = 2, moveTargetY = 4, attackTargetId = 14)),
            NormalMove(MoveAndAssist(heroUnitId = 1, moveTargetX = 1, moveTargetY = 4, assistTargetId = 2)),
            NormalMove(MoveAndAssist(heroUnitId = 3, moveTargetX = 2, moveTargetY = 3, assistTargetId = 1)),
            NormalMove(MoveAndAttack(heroUnitId = 2, moveTargetX = 1, moveTargetY = 5, attackTargetId = 15)),
            NormalMove(MoveAndAssist(heroUnitId = 3, moveTargetX = 3, moveTargetY = 4, assistTargetId = 1)),
            NormalMove(MoveAndAssist(heroUnitId = 1, moveTargetX = 1, moveTargetY = 4, assistTargetId = 2)),
            NormalMove(MoveAndAttack(heroUnitId = 4, moveTargetX = 2, moveTargetY = 4, attackTargetId = 11)),
            NormalMove(MoveAndAttack(heroUnitId = 2, moveTargetX = 1, moveTargetY = 3, attackTargetId = 18)),
            NormalMove(MoveAndAssist(heroUnitId = 3, moveTargetX = 1, moveTargetY = 4, assistTargetId = 1)),
            NormalMove(MoveAndAttack(heroUnitId = 4, moveTargetX = 2, moveTargetY = 4, attackTargetId = 16)),
            NormalMove(MoveAndAttack(heroUnitId = 1, moveTargetX = 2, moveTargetY = 5, attackTargetId = 6))
        ),
//        startingMoves = sequenceOf(
//            NormalMove(MoveOnly(heroUnitId = 3, moveTargetX = 4, moveTargetY = 3)),
//            NormalMove(MoveAndAssist(heroUnitId = 4, moveTargetX = 3, moveTargetY = 3, assistTargetId = 3)),
//            NormalMove(MoveAndAttack(heroUnitId = 3, moveTargetX = 4, moveTargetY = 1, attackTargetId = 10)),
//            NormalMove(MoveAndAttack(heroUnitId = 1, moveTargetX = 3, moveTargetY = 2, attackTargetId = 9)),
//            NormalMove(MoveOnly(heroUnitId = 2, moveTargetX = 5, moveTargetY = 3))            ,
//
//            NormalMove(MoveOnly(heroUnitId = 3, moveTargetX = 4, moveTargetY = 3)),
//            NormalMove(MoveAndAssist(heroUnitId = 4, moveTargetX = 4, moveTargetY = 2, assistTargetId = 3)),
//            NormalMove(MoveAndAttack(heroUnitId = 2, moveTargetX = 5, moveTargetY = 1, attackTargetId = 13)),
//            NormalMove(MoveAndAttack(heroUnitId = 1, moveTargetX = 5, moveTargetY = 2, attackTargetId = 13)),
//            NormalMove(MoveOnly(heroUnitId = 3, moveTargetX = 4, moveTargetY = 3)),
//
//            NormalMove(MoveOnly(heroUnitId = 2, moveTargetX = 5, moveTargetY = 3)),
//            NormalMove(MoveAndAttack(heroUnitId = 3, moveTargetX = 4, moveTargetY = 4, attackTargetId = 12)),
//            NormalMove(MoveAndAttack(heroUnitId = 1, moveTargetX = 5, moveTargetY = 5, attackTargetId = 14)),
//            NormalMove(MoveAndAssist(heroUnitId = 4, moveTargetX = 5, moveTargetY = 4, assistTargetId = 1)),
//            NormalMove(MoveAndAttack(heroUnitId = 1, moveTargetX = 5, moveTargetY = 5, attackTargetId = 14))
//
////            NormalMove(MoveOnly(heroUnitId = 1, moveTargetX = 5, moveTargetY = 6)),
////            NormalMove(MoveAndAttack(heroUnitId = 3, moveTargetX = 4, moveTargetY = 5, attackTargetId = 17)),
////            NormalMove(MoveAndAssist(heroUnitId = 4, moveTargetX = 5, moveTargetY = 5, assistTargetId = 3)),
////            NormalMove(MoveAndAttack(heroUnitId = 3, moveTargetX = 4, moveTargetY = 5, attackTargetId = 17)),
////            NormalMove(MoveOnly(heroUnitId = 2, moveTargetX = 5, moveTargetY = 6))
//
//        ),
                hardPhaseLimit = 20,
        canRearrange = false
    )
    runBlocking {
        val jobInfo = jobConfig.toJobInfo()
        jobInfo.startNewJob()
        jobInfo.completableJob.join()
    }
}


