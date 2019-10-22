package com.bloombase.feh

class BattleState private constructor(
    private val battleMap: BattleMap,
    private val forwardMap: MutableMap<Position, ChessPiece>,
    phrase: Int
) {
    enum class MovementResult {
        PLAYER_WIN,
        PLAYER_UNIT_DIED,
        PHRASE_CHANGE,
        NOTHING
    }

    private enum class FightResult(val movementResult: MovementResult?) {
        PLAYER_WIN(MovementResult.PLAYER_WIN),
        PLAYER_UNIT_DIED(MovementResult.PLAYER_UNIT_DIED),
        ENEMY_UNIT_DIED(null),
        NOTHING(null)
    }

    private val maxX: Int = battleMap.size.x - 1
    private val maxY: Int = battleMap.size.y - 1

    var phrase = phrase
        private set
    val reverseMap = mutableMapOf<HeroUnit, Position>()
    private val unitIdMap = forwardMap.values.filterIsInstance<HeroUnit>().associateBy { it.id }
    val playerUnits = reverseMap.keys.asSequence().filter { it.team == Team.PLAYER }
    private val enemyUnits = reverseMap.keys.asSequence().filter { it.team == Team.ENEMY }

    fun copy(): BattleState {
        val newForwardMap = mutableMapOf<Position, ChessPiece>()
        forwardMap.mapValuesTo(newForwardMap) { it.value.copy() }
        return BattleState(battleMap, newForwardMap, phrase)
    }

    init {
        forwardMap.entries.asSequence().mapNotNull {
            if (it.value is HeroUnit) {
                Pair(it.value as HeroUnit, it.key)
            } else {
                null
            }
        }.associateTo(reverseMap) { it }
    }

    constructor(battleMap: BattleMap) : this(battleMap, battleMap.toChessPieceMap().toMutableMap(), 0) {
        startOfTurn(Team.PLAYER)
    }

    fun unitsAndPos(team: Team): Sequence<Map.Entry<HeroUnit, Position>> {
        return reverseMap.asSequence().filter { it.key.team == team }
    }

    private fun executeMove(unitMovement: UnitMovement): FightResult {
        return executeMove(
            unitMovement.heroUnitId,
            unitMovement.move,
            unitMovement.attackTargetId,
            unitMovement.assistTargetId
        )
    }

    private fun executeMove(
        heroUnitId: Int,
        movePosition: Position,
        attackTargetId: Int?,
        assistTargetId: Int?
    ): FightResult {
        val heroUnit = getUnit(heroUnitId)
        move(heroUnit, movePosition)
        return if (attackTargetId != null) {
            val target = getUnit(attackTargetId)
            fight(heroUnit, target)
        } else {
            standStill(heroUnit)
            FightResult.NOTHING
        }
    }

    private fun turnEnd() {
        val nextTeam = if (++phrase % 2 == 0) {
            Team.PLAYER
        } else {
            Team.ENEMY
        }
        startOfTurn(nextTeam)
    }

    private fun move(heroUnit: HeroUnit, position: Position) {
        val originalPosition = reverseMap.put(heroUnit, position) ?: throw IllegalStateException()
        if (originalPosition != position) {
            forwardMap.remove(originalPosition)
            val piece = forwardMap.put(position, heroUnit)
            check(piece == null) {
                piece.toString()
            }
        }
    }

    private fun standStill(heroUnit: HeroUnit) {
        heroUnit.endOfTurn()
    }

    private fun startOfTurn(team: Team): List<HeroUnit> {
        val units = units(team)
        units.forEach(HeroUnit::startOfTurn)
        units.forEach { heroUnit ->
            heroUnit.skillSet.startOfTurn.forEach {
                it.apply(this, heroUnit)
            }
        }
        return units
    }

    private fun units(team: Team) = reverseMap.keys.asSequence().filter { it.team == team }.toList()

    private fun fight(attacker: HeroUnit, defender: HeroUnit): FightResult {
        val attackerStat = attacker.stat + attacker.buff + attacker.debuff + skillMethodSumStat(
            attacker,
            defender,
            SkillSet::buffSkill,
            SkillSet::debuffSkill
        )
        val defenderStat = defender.stat + defender.buff + defender.debuff + skillMethodSumStat(
            attacker,
            defender,
            SkillSet::debuffSkill,
            SkillSet::buffSkill
        )

        val spdDiff = attackerStat.spd - defenderStat.spd

        val attackOrder = createAttackOrder(attacker, defender, spdDiff)

        val enemyDied = attackOrder.any { attackerTurn ->
            val deadTeam = if (attackerTurn) {
                if (singleAttack(attacker, defender, attackerStat, defenderStat)) {
                    defender.team
                } else {
                    null
                }
            } else {
                if (singleAttack(defender, attacker, defenderStat, attackerStat)) {
                    attacker.team
                } else {
                    null
                }
            }
            when (deadTeam) {
                Team.PLAYER -> return FightResult.PLAYER_UNIT_DIED
                Team.ENEMY -> {
                    if (enemyUnits.none()) {
                        return FightResult.PLAYER_WIN
                    }
                    true
                }
                null -> false
            }
        }

        if (!attacker.isDead) {
            attacker.endOfTurn()
        }
        skillMethodApply(attacker, defender, SkillSet::postCombat)
        return if (enemyDied) FightResult.ENEMY_UNIT_DIED else FightResult.NOTHING
    }

    private fun singleAttack(
        attacker: HeroUnit,
        defender: HeroUnit,
        attackerStat: Stat,
        defenderStat: Stat
    ): Boolean {
        val dead = defender.takeDamage(calculateDamage(attacker, defender, attackerStat, defenderStat))
        val attackerCooldownIncrease =
            attacker.skillSet.cooldownBuff.asSequence().map { it.apply(this, attacker, defender, true) }.max() ?: 0
        val attackerCooldownReduce =
            defender.skillSet.cooldownDebuff.asSequence().map { it.apply(this, defender, attacker, false) }.max() ?: 0

        attacker.reduceCooldown(1 + attackerCooldownIncrease - attackerCooldownReduce)
        if (!dead) {
            val defenderCooldownIncrease =
                attacker.skillSet.cooldownBuff.asSequence().map { it.apply(this, defender, attacker, false) }.max() ?: 0
            val defenderCooldownReduce =
                defender.skillSet.cooldownDebuff.asSequence().map { it.apply(this, attacker, defender, true) }.max()
                    ?: 0
            defender.reduceCooldown(1 + defenderCooldownIncrease - defenderCooldownReduce)
        } else {
            val position = reverseMap.remove(defender) ?: throw IllegalStateException()
            forwardMap.remove(position)
        }
        return dead
    }

    private fun createAttackOrder(
        attacker: HeroUnit,
        defender: HeroUnit,
        spdDiff: Int
    ): MutableList<Boolean> {
        val rangeMatch = when {
            attacker.isEmptyHanded -> false
            attacker.weaponType.isRanged == defender.weaponType.isRanged -> true
            else -> skillMethodAny(attacker, defender, SkillSet::ignoreRange)
        }

        val counter = rangeMatch

        val disablePriorityChange = skillMethodAny(attacker, defender, SkillSet::disablePriorityChange)

        val desperation: Boolean
        val vantage: Boolean
        if (disablePriorityChange) {
            desperation = false
            vantage = false
        } else {
            desperation = skillMethodAny(attacker, defender, SkillSet::desperation)
            vantage = skillMethodAny(attacker, defender, SkillSet::vantage)
        }

        val attackerFollowup = when (val guarantee =
            skillMethodSum(attacker, defender, SkillSet::followUpSelf, SkillSet::followUpOpponent)) {
            0 -> spdDiff >= 5
            else -> guarantee > 0
        }
        val defenderFollowup = when (val guarantee =
            skillMethodSum(attacker, defender, SkillSet::followUpOpponent, SkillSet::followUpSelf)) {
            0 -> spdDiff <= -5
            else -> guarantee > 0
        }

        val attackOrder = mutableListOf<Boolean>()

        if (vantage) {
            if (counter) {
                attackOrder.add(false)
            }
            attackOrder.add(true)
        } else {
            attackOrder.add(true)
        }

        if (attackerFollowup && desperation) {
            attackOrder.add(true)
        }

        if (!vantage) {
            if (counter) {
                attackOrder.add(false)
            }
        }

        if (attackerFollowup && !desperation) {
            attackOrder.add(true)
        }

        if (defenderFollowup) {
            if (counter) {
                attackOrder.add(false)
            }
        }
        return attackOrder
    }


    private fun calculateDamage(
        attacker: HeroUnit,
        defender: HeroUnit,
        attackerStat: Stat,
        defenderStat: Stat
    ): Int {
        val targetRes = attacker.weaponType.targetRes
        val defenderDefRes = if (targetRes) {
            defenderStat.def
        } else {
            defenderStat.res
        }
        val effAtk = if (isEffective(attacker, defender)) {
            attackerStat.atk * 3 / 2
        } else {
            attackerStat.atk
        }
        val colorAdvantage = attacker.getColorAdvantage(defender)
        val atk = if (colorAdvantage != 0) {
            effAtk + effAtk * colorAdvantage / 100
        } else {
            effAtk
        }

        return atk - defenderDefRes
    }

    private fun isEffective(attacker: HeroUnit, defender: HeroUnit): Boolean {
        if (attacker.weaponType == Bow && defender.moveType == MoveType.FLYING) {
            return true
        }
        return false
    }

    fun playerMove(unitMovement: UnitMovement): MovementResult {
        check(isPlayerPhrase)
        val movementResult = executeMove(unitMovement).movementResult
        if (movementResult != null) {
            return movementResult
        }
        if (playerUnits.none { it.available }) {
            turnEnd()
            return MovementResult.PHRASE_CHANGE
        }
        return MovementResult.NOTHING
    }

    private val isPlayerPhrase
        get() = phrase % 2 == 0

    fun enemyMoves(): List<UnitMovement> {
        val movementRanges = units(Team.PLAYER).associateWith {
            it.travelPower
        }

        val obstacles = forwardMap.toMutableMap()

        val distanceFromEnemy = unitsAndPos(Team.PLAYER).filterNot { it.key.isEmptyHanded }.associate {
            val resultMap = mutableMapOf<Position, Int>()
            calculateDistance(it.key, it.value, object : DistanceReceiver {
                override fun isOverMaxDistance(distance: Int): Boolean {
                    return false
                }

                override fun receive(moveStep: MoveStep): Boolean {
                    return resultMap.putIfAbsent(moveStep.position, moveStep.distanceTravel) == null
                }

            })
            it.key to resultMap.toMap()
        }

        val enemyThreat = calculateThreat(movementRanges, obstacles, Team.PLAYER)

        val comparator = positionComparator(enemyThreat)

        val movements = unitsAndPos(Team.ENEMY).map {
            val move = EnemyMovement(
                heroUnit = it.key.id,
                move = it.value,
                attack = null,
                assist = null
            )
            executeMove(move)
            move
        }.toList()

        val attackTargets = unitsAndPos(Team.ENEMY).filter { it.key.available }.filterNot { it.key.isEmptyHanded }
            .associateWith { (heroUnit, position) ->
                moveTargets(heroUnit, position).sortedWith(comparator).flatMap { moveStep ->
                    attackTargets(heroUnit, moveStep.position).map {
                        moveStep to it
                    }
                }.distinctBy { it.second }.map { (movePosition, foe) ->
                    val testBattle = copy()
                    val heroUnitId = heroUnit.id
                    val foeId = foe.id
                    val winLoss = when (testBattle.executeMove(heroUnitId, movePosition.position, foeId, null)) {
                        FightResult.PLAYER_UNIT_DIED -> -1
                        FightResult.PLAYER_WIN, FightResult.ENEMY_UNIT_DIED -> 1
                        FightResult.NOTHING -> 0
                    }
                    val testUnit = testBattle.getUnit(heroUnitId)
                    val testFoe = testBattle.getUnit(foeId)
                    // warning - not sure if this is correct
                    val debuffSuccess = if (heroUnit.debuffer > 0) {
                        if (testFoe.debuff.def + testFoe.debuff.res + 2 <= foe.debuff.def + foe.debuff.res) {
                            -heroUnit.debuffer
                        } else {
                            0
                        }
                    } else {
                        0
                    }
                    val damageDealt = foe.currentHp - testFoe.currentHp
                    val damageReceived = heroUnit.currentHp - testUnit.currentHp
                    val cooldownChange = (testUnit.cooldownCount ?: 0) - (heroUnit.cooldownCount ?: 0)
                    val cooldownChangeFoe = (testFoe.cooldownCount ?: 0) - (foe.cooldownCount ?: 0)
                    CombatResult(
                        heroUnit,
                        position,
                        foe,
                        winLoss,
                        debuffSuccess,
                        damageDealt,
                        damageReceived,
                        cooldownChange,
                        cooldownChangeFoe
                    )
                }.sortedWith(bestAttackTarget).firstOrNull()
            }

        turnEnd()
        return movements
    }

    private fun calculateThreat(
        movementRanges: Map<HeroUnit, Int>,
        obstacles: MutableMap<Position, ChessPiece>,
        team: Team
    ): Map<Position, Int> {
        return unitsAndPos(team).filterNot { it.key.isEmptyHanded }.flatMap { (heroUnit, position) ->
            val pass = heroUnit.skillSet.pass.any { it.apply(this, heroUnit) }
            val movementRange = movementRanges[heroUnit] ?: throw IllegalStateException()
            val threatReceiver = if (pass) {
                ThreatWithPass(movementRange, obstacles, team)
            } else {
                ThreatWithoutPass(movementRange, obstacles)
            }
            calculateDistance(heroUnit, position, threatReceiver)
            threatReceiver.result.flatMap {
                if (heroUnit.isEmptyHanded) {
                    emptySequence()
                } else {
                    val isRanged = heroUnit.weaponType.isRanged
                    if (isRanged) {
                        it.surroundings.flatMap { it.surroundings }
                    } else {
                        it.surroundings
                    }
                }
            }.distinct()
        }.groupingBy { it }.eachCount()
    }

    private fun getUnit(heroUnitId: Int) =
        unitIdMap[heroUnitId] ?: throw IllegalStateException()

    private fun checkObstacle(
        position: Position,
        team: Team,
        obstacles: Map<Position, ChessPiece>
    ): Boolean? {
        return when (val obstacle = obstacles[position]) {
            is Obstacle -> null
            is HeroUnit -> if (obstacle.team == team) {
                true
            } else {
                null
            }
            null -> false
        }
    }

    fun moveTargets(
        heroUnit: HeroUnit,
        position: Position = reverseMap[heroUnit] ?: throw IllegalArgumentException()
    ): Sequence<MoveStep> {
        val pass = heroUnit.skillSet.pass.any { it.apply(this, heroUnit) }
        val travelPower = heroUnit.travelPower
        val obstacles = forwardMap
        val team = heroUnit.team
        val distanceReceiver = DistanceReceiverRealMovement(travelPower, obstacles, team, pass)
        calculateDistance(
            heroUnit,
            position,
            distanceReceiver
        )
        return distanceReceiver.result
    }

    private fun <K, V> MutableMap<K, Sequence<V>>.add(k: K, values: Sequence<V>) {
        val v = this[k]
        this[k] = if (v == null) {
            values
        } else {
            v + values
        }
    }

    private fun calculateDistance(
        heroUnit: HeroUnit,
        heroUnitPosition: Position,
        distanceReceiver: DistanceReceiver
    ) {
        val moveType = heroUnit.moveType
        val workingMap = sortedMapOf(
            0 to sequenceOf(MoveStep(heroUnitPosition, battleMap.getTerrain(heroUnitPosition), false, 0))
        )

        while (workingMap.isNotEmpty()) {
            val currentDistance = workingMap.firstKey()
            if (distanceReceiver.isOverMaxDistance(currentDistance)) {
                break
            }
            val temp = workingMap.remove(currentDistance) ?: throw IllegalStateException()
            temp.asSequence().filter {
                distanceReceiver.receive(it)
            }.flatMap { it.position.surroundings }.mapNotNull { position ->
                val terrain = battleMap.getTerrain(position)
                val moveCost = terrain.moveCost(moveType) ?: return@mapNotNull null
                val distanceTravel = currentDistance + moveCost
                distanceTravel to MoveStep(position, terrain, false, distanceTravel)
            }.groupBy({ it.first }, { it.second }).forEach { (distance, list) ->
                workingMap.add(distance, list.asSequence())
            }
        }
    }

    fun attackTargets(
        heroUnit: HeroUnit,
        position: Position
    ): Sequence<HeroUnit> {
        if (heroUnit.isEmptyHanded) {
            return emptySequence()
        }
        val range = heroUnit.weaponType.range

        return unitsAndPos(heroUnit.team.opponent).filter { it.value.distanceTo(position) == range }.map { it.key }
    }

    private val Position.surroundings: Sequence<Position>
        get() {
            return sequence {
                if (x > 0) {
                    yield(Position(x - 1, y))
                }
                if (y > 0) {
                    yield(Position(x, y - 1))
                }
                if (x < maxX) {
                    yield(Position(x + 1, y))
                }
                if (y < maxY) {
                    yield(Position(x, y + 1))
                }
            }
        }

    private inline fun skillMethodAny(
        attacker: HeroUnit,
        defender: HeroUnit,
        f: (SkillSet) -> List<CombatSkillMethod<Boolean>>
    ) =
        f(attacker.skillSet).any { it.apply(this, attacker, defender, true) } ||
                f(defender.skillSet).any { it.apply(this, defender, attacker, false) }

    private inline fun skillMethodApply(
        attacker: HeroUnit,
        defender: HeroUnit,
        f: (SkillSet) -> List<CombatSkillMethod<Unit>>
    ) {
        f(attacker.skillSet).forEach { it.apply(this, attacker, defender, true) }
        f(defender.skillSet).forEach { it.apply(this, defender, attacker, false) }
    }

    private inline fun skillMethodSum(
        attacker: HeroUnit,
        defender: HeroUnit,
        f: (SkillSet) -> List<CombatSkillMethod<Int>>,
        g: (SkillSet) -> List<CombatSkillMethod<Int>>
    ) =
        f(attacker.skillSet).sumBy { it.apply(this, attacker, defender, true) } +
                g(defender.skillSet).sumBy { it.apply(this, defender, attacker, false) }

    private inline fun skillMethodSumStat(
        attacker: HeroUnit,
        defender: HeroUnit,
        f: (SkillSet) -> List<CombatSkillMethod<Stat>>,
        g: (SkillSet) -> List<CombatSkillMethod<Stat>>
    ) =
        f(attacker.skillSet).asSequence().map {
            it.apply(
                this,
                attacker,
                defender,
                true
            )
        }.ifEmpty { sequenceOf(Stat.ZERO) }.reduce(Stat::plus) +
                g(defender.skillSet).asSequence().map {
                    it.apply(
                        this,
                        defender,
                        attacker,
                        false
                    )
                }.ifEmpty { sequenceOf(Stat.ZERO) }.reduce(Stat::plus)

    private fun positionComparator(enemyThreat: Map<Position, Int>) = compareBy<MoveStep>(
        {
            if (it.terrain == Terrain.DEFENSE_TILE) 0 else 1
        },
        {
            if (it.teleportRequired) 0 else 1
        },
        {
            enemyThreat[it.position] ?: 0
        },
        {
            it.terrain
        },
        {
            it.distanceTravel
        },
        {
            it.position
        }
    )
}

class DistanceReceiverRealMovement(
    private val travelPower: Int,
    private val obstacles: MutableMap<Position, ChessPiece>,
    private val selfTeam: Team,
    private val pass: Boolean
) : DistanceReceiver {
    private val resultMap = mutableMapOf<Position, MoveStep>()
    override fun isOverMaxDistance(distance: Int): Boolean {
        return distance > travelPower
    }
    val result
        get() = resultMap.values.asSequence().filter {
            it.distanceTravel > 0
        }

    override fun receive(moveStep: MoveStep): Boolean {
        val position = moveStep.position
        if (resultMap[position] != null) {
            return false
        }
        return when (val obstacle = obstacles[position]) {
            is Obstacle -> {
                resultMap[position] = moveStep.copy(distanceTravel = -1)
                false
            }
            is HeroUnit -> {
                if (obstacle.team == selfTeam) {
                    resultMap[position] = moveStep.copy(distanceTravel = -1)
                    true
                } else {
                    resultMap[position] = moveStep.copy(distanceTravel = -1)
                    pass
                }
            }
            null -> {
                resultMap[position] = moveStep
                true
            }
        }
    }

}

private interface ThreatReceiver : DistanceReceiver {
    val result: Sequence<Position>
}

private class ThreatWithoutPass(
    private val movementRange: Int,
    private val obstacles: MutableMap<Position, ChessPiece>
) : ThreatReceiver {
    private val resultMap = mutableMapOf<Position, Boolean>()
    override val result
        get() = resultMap.asSequence().filter { it.value }.map { it.key }

    override fun isOverMaxDistance(distance: Int): Boolean {
        return distance > movementRange
    }

    override fun receive(moveStep: MoveStep): Boolean {
        if (resultMap[moveStep.position] != null) {
            return false
        }
        val isObstacle = obstacles[moveStep.position] is Obstacle
        resultMap[moveStep.position] = isObstacle
        return isObstacle
    }
}

private class ThreatWithPass(
    private val movementRange: Int,
    private val obstacles: MutableMap<Position, ChessPiece>,
    private val selfTeam: Team
) : ThreatReceiver {
    private val resultMap = mutableMapOf<Position, Boolean>()
    override val result
        get() = resultMap.asSequence().filter { it.value }.map { it.key }

    override fun isOverMaxDistance(distance: Int): Boolean {
        return distance > movementRange
    }

    override fun receive(moveStep: MoveStep): Boolean {
        val position = moveStep.position
        if (resultMap[position] != null) {
            return false
        }
        return when (val obstacle = obstacles[position]) {
            is Obstacle -> {
                resultMap[position] = false
                false
            }
            is HeroUnit -> {
                if (obstacle.team == selfTeam) {
                    resultMap[position] = true
                    true
                } else {
                    resultMap[position] = false
                    true
                }
            }
            null -> {
                resultMap[position] = true
                true
            }
        }
    }
}

private class CombatResult(
    val heroUnit: HeroUnit,
    val position: Position,
    val foe: HeroUnit,
    val winLoss: Int,
    val debuffSuccess: Int,
    val damageDealt: Int,
    val damageReceived: Int,
    val cooldownChange: Int,
    val cooldownChangeFoe: Int
) {
    val damageRatio = -(damageDealt * 3 - damageReceived)
}

private val bestAttackTarget = compareBy<CombatResult>(
    {
        it.winLoss
    },
    {
        -it.debuffSuccess
    },
    {
        it.damageRatio
    },
    {
        if (it.cooldownChange > 0) {
            0
        } else {
            1
        }
    },
    {
        it.foe.id
    }
)

private val bestAttacker = compareBy<CombatResult>(
    {
        it.winLoss
    },
    {
        -it.debuffSuccess
    },
    {
        if (it.heroUnit.hasSpecialDebuff) {
            0
        } else {
            1
        }
    },
    {
        it.damageRatio
    },
    {
        it.heroUnit.travelPower
    },
    {
        if (it.cooldownChangeFoe > 0) {
            0
        } else {
            1
        }
    },
    {
        it.foe.id
    }
)


data class MoveStep(val position: Position, val terrain: Terrain, val teleportRequired: Boolean, val distanceTravel: Int) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MoveStep) return false

        if (position != other.position) return false

        return true
    }

    override fun hashCode(): Int {
        return position.hashCode()
    }
}

private class MoveStepTemp(
    val position: Position,
    val passThroughOnly: Boolean,
    val terrain: Terrain,
    val distanceTravel: Int
) {
    fun toMoveStep(): MoveStep? {
        return if (passThroughOnly) {
            null
        } else {
            MoveStep(position, terrain, false, distanceTravel)
        }
    }
}

private interface DistanceReceiver {

    fun isOverMaxDistance(distance: Int): Boolean

    fun receive(moveStep: MoveStep): Boolean
}