package me.kumatheta.ws

import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.routing.delete
import io.ktor.routing.get
import io.ktor.routing.put
import io.ktor.routing.routing
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.serialization.protobuf.ProtoBuf
import me.kumatheta.feh.*
import me.kumatheta.feh.mcts.*
import me.kumatheta.feh.message.*
import me.kumatheta.mcts.*
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference
import kotlin.time.ExperimentalTime

typealias MsgTerrain = me.kumatheta.feh.message.Terrain
typealias MsgTerrainType = me.kumatheta.feh.message.Terrain.Type
typealias MsgBattleMap = me.kumatheta.feh.message.BattleMap
typealias MsgMoveType = me.kumatheta.feh.message.MoveType

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@ExperimentalTime
private val jobConfig = FehJobConfig(
//    scoreManagerFactory = UCT<FehMove>().toFactory(),
//    scoreManagerFactory = LocalVaryingUCT<FehMove>(1.5).toFactory(),
//    scoreManagerFactory = DynamicUCTTuned<FehMove>().toFactory(),
    scoreManagerFactory = hybridDynamicUCTTune<FehMove>(), //hybridDynamicUCTTune<FehMove>(),
    mapName = "robin f ghb abyssal"
//    moveDownCriteria = MoveDownCriteria(null, 1000000, 600000)
//    toInternalBattleMap = {
//        NoCacheBattleMap(this)
//    }
)

@ExperimentalCoroutinesApi
@ExperimentalTime
@Suppress("unused") // Referenced in application.conf
fun Application.module() {
    val protoBuf = ProtoBuf()
    val jobInfoRef = AtomicReference<FehJobInfo<Score<FehMove>>?>(null)

    routing {
        get("/job") {
            val jobInfo = getOrStartJob(jobConfig, jobInfoRef)
            call.respond(protoBuf.dump(SetupInfo.serializer(), jobInfo.setupInfo))
        }
        put("/job") {
            val jobInfo = restartJob(jobConfig, jobInfoRef)
            call.respond(protoBuf.dump(SetupInfo.serializer(), jobInfo.setupInfo))
        }
        delete("/job") {
            jobInfoRef.get()?.completableJob?.cancel()
            call.respond("cancel success")
        }
        get("/job/moveSet") {
            val jobInfo = jobInfoRef.get()
            if (jobInfo == null) {
                call.respond(HttpStatusCode.BadRequest, "no job running")
                return@get
            }
            val isCompleted = jobInfo.completableJob.isCompleted
            val mcts = jobInfo.mcts
            val currentScore = if (isCompleted) {
                mcts.score
            } else {
                resetScoreWithRetry(mcts)
            }
            val elapsed: Long = if (isCompleted) {
                jobInfo.elapsed.get()
            } else {
                jobInfo.startTime.elapsedNow().toLong(TimeUnit.SECONDS)
            }
            val moves = currentScore.moves
            if (moves == null) {
                call.respond(HttpStatusCode.BadRequest)
                return@get
            }
            val (testState, updates) = toUpdateInfoList(jobInfo.board, moves)

            val allTimeBest = mcts.score
            val runtime = Runtime.getRuntime()
            val moveSet = MoveSet(
                updates,
                currentScore.bestScore,
                currentScore.tries,
                testState.enemyDied,
                testState.playerDied,
                allTimeBest.bestScore,
                allTimeBest.tries,
                mcts.estimatedSize,
                runtime.totalMemory() - runtime.freeMemory(),
                elapsed,
                isCompleted
            )

            call.respond(protoBuf.dump(MoveSet.serializer(), moveSet))
        }
    }
}

private val NULL_ACTION = Action(-1, -1, -1, -1, -1, -1)

fun toUpdateInfoList(
    board: FehBoard,
    moves: List<FehMove>
): Pair<BattleState, List<UpdateInfo>> {
    var lastState = board.getStateCopy()
    val details = board.tryAndGetDetails(moves)
    val list = details.map { (unitAction, state) ->
        val action = unitAction?.toMsgAction()
        val oldUnits = (lastState.unitsSeq(Team.PLAYER) + lastState.unitsSeq(Team.ENEMY)).associateBy { it.id }
        val newUnits = (state.unitsSeq(Team.PLAYER) + state.unitsSeq(Team.ENEMY)).associateBy { it.id }
        val unitsUpdated = getUpdated(oldUnits, newUnits).toList()
        val unitsAdded =
            newUnits.values.asSequence().filterNot { oldUnits.containsKey(it.id) }.map(HeroUnit::toUnitAdded)
                .toList()
        lastState = state
        UpdateInfo(action ?: NULL_ACTION, unitsUpdated, unitsAdded)
    }
    return lastState to list
}

@ExperimentalCoroutinesApi
@ExperimentalTime
private fun <S : Score<FehMove>, M : ScoreManagerFactory<FehMove, S>> getOrStartJob(
    jobConfig: FehJobConfig<S, M>,
    jobInfoRef: AtomicReference<FehJobInfo<Score<FehMove>>?>
): FehJobInfo<Score<FehMove>> {
    val jobInfo = jobInfoRef.get()
    if (jobInfo != null) {
        return jobInfo
    }
    val newJobInfo = jobConfig.toJobInfo()
    do {
        val prev = jobInfoRef.get()
        if (prev != null) {
            return prev
        }
    } while (!jobInfoRef.compareAndSet(null, newJobInfo))

    newJobInfo.startNewJob()
    return newJobInfo
}

@ExperimentalCoroutinesApi
@ExperimentalTime
private fun <S : Score<FehMove>, M : ScoreManagerFactory<FehMove, S>> restartJob(
    jobConfig: FehJobConfig<S, M>,
    jobInfoRef: AtomicReference<FehJobInfo<Score<FehMove>>?>
): FehJobInfo<Score<FehMove>> {
    val newJobInfo = jobConfig.toJobInfo()
    val oldJobInfo = jobInfoRef.getAndSet(newJobInfo)
    oldJobInfo?.completableJob?.cancel()

    newJobInfo.startNewJob()
    return newJobInfo
}

private fun getUpdated(
    oldUnits: Map<Int, HeroUnit>,
    newUnits: Map<Int, HeroUnit>
): Sequence<UnitUpdate> {
    return oldUnits.values.asSequence().mapNotNull { old ->
        val new = newUnits[old.id]
        if (new == null) {
            UnitUpdate(old.id, 0, false, 0, 0)
        } else {
            if (new.currentHp == old.currentHp && new.available == old.available && new.position == old.position) {
                null
            } else {
                UnitUpdate(old.id, new.currentHp, new.available, new.position.x, new.position.y)
            }
        }
    }
}

private fun UnitAction.toMsgAction(): Action {
    return when (this) {
        is MoveOnly -> Action(heroUnitId, moveTarget.x, moveTarget.y, -1, -1, -1)
        is MoveAndAttack -> Action(heroUnitId, moveTarget.x, moveTarget.y, attackTargetId, -1, -1)
        is MoveAndBreak -> Action(heroUnitId, moveTarget.x, moveTarget.y, -1, obstacle.x, obstacle.y)
        is MoveAndAssist -> Action(heroUnitId, moveTarget.x, moveTarget.y, assistTargetId, -1, -1)
    }
}

@ExperimentalTime
@ExperimentalCoroutinesApi
private suspend fun <S : Score<FehMove>> resetScoreWithRetry(mcts: Mcts<FehMove, S>): S {
    repeat(10) {
        val score = mcts.resetRecentScore()
        val moves = score.moves
        if (moves != null) {
            return score
        }
        println("delay response")
        delay(50)
    }
    return mcts.resetRecentScore()
}


