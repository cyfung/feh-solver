package me.kumatheta.feh

class BattleState private constructor(
    private val battleMap: BattleMap,
    private val locationMap: MutableMap<Position, ChessPiece>,
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
    private val unitIdMap = locationMap.values.filterIsInstance<HeroUnit>().associateBy { it.id }

    fun copy(): BattleState {
        val newForwardMap = mutableMapOf<Position, ChessPiece>()
        locationMap.mapValuesTo(newForwardMap) { it.value.copy() }
        return BattleState(battleMap, newForwardMap, phrase)
    }

    constructor(battleMap: BattleMap) : this(battleMap, battleMap.toChessPieceMap().toMutableMap(), 0) {
        startOfTurn(Team.PLAYER)
    }

    fun unitsSeq(team: Team) =
        locationMap.values.asSequence().filterIsInstance<HeroUnit>().filter { it.team == team }

    private fun executeMove(unitAction: UnitAction): FightResult {
        val heroUnit = getUnit(unitAction.heroUnitId)
        move(heroUnit, unitAction.moveTarget)
        return when (unitAction) {
            is MoveOnly -> {
                standStill(heroUnit)
                FightResult.NOTHING
            }
            is MoveAndAttack -> {
                val target = getUnit(unitAction.attackTargetId)
                fight(heroUnit, target).first
            }
            is MoveAndAssist -> {
                val assist = heroUnit.assist ?: throw IllegalStateException()
                val target = getUnit(unitAction.assistTargetId)
                assist.apply(heroUnit, target)
                FightResult.NOTHING
            }
        }
    }

    private fun moveAndFight(
        heroUnitId: Int,
        movePosition: Position,
        attackTargetId: Int
    ): Pair<FightResult, Int> {
        val heroUnit = getUnit(heroUnitId)
        move(heroUnit, movePosition)
        val target = getUnit(attackTargetId)
        return fight(heroUnit, target)
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
        val originalPosition = heroUnit.position
        if (originalPosition != position) {
            heroUnit.position = position
            locationMap.remove(originalPosition)
            val piece = locationMap.put(position, heroUnit)
            check(piece == null) {
                piece.toString()
            }
        }
    }

    private fun standStill(heroUnit: HeroUnit) {
        heroUnit.endOfTurn()
    }

    private fun startOfTurn(team: Team): List<HeroUnit> {
        val units = unitsSeq(team).toList()
        units.forEach(HeroUnit::startOfTurn)
        units.forEach { heroUnit ->
            heroUnit.skillSet.startOfTurn.forEach {
                it.apply(this, heroUnit)
            }
        }
        return units
    }

    private fun fight(attacker: HeroUnit, defender: HeroUnit): Pair<FightResult, Int> {
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

        val potentialDamage = attackOrder.asSequence().filter { true }.count() * calculateDamage(
            attacker,
            defender,
            attackerStat,
            defenderStat
        )

        val deadTeam = attackOrder.asSequence().mapNotNull { attackerTurn ->
            if (attackerTurn) {
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
        }.firstOrNull()


        attacker.endOfTurn()

        skillMethodApply(attacker, defender, SkillSet::postCombat)

        val fightResult = when (deadTeam) {
            Team.PLAYER -> FightResult.PLAYER_UNIT_DIED
            Team.ENEMY -> {
                if (unitsSeq(Team.ENEMY).none()) {
                    FightResult.PLAYER_WIN
                } else {
                    FightResult.ENEMY_UNIT_DIED
                }
            }
            null -> FightResult.NOTHING
        }
        return Pair(fightResult, potentialDamage)
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
            locationMap.remove(defender.position)
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

        val canCounter = rangeMatch

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
            if (canCounter) {
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
            if (canCounter) {
                attackOrder.add(false)
            }
        }

        if (attackerFollowup && !desperation) {
            attackOrder.add(true)
        }

        if (defenderFollowup) {
            if (canCounter) {
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

    fun playerMove(unitAction: UnitAction): MovementResult {
        check(isPlayerPhrase)
        val movementResult = executeMove(unitAction).movementResult
        if (movementResult != null) {
            return movementResult
        }
        if (unitsSeq(Team.PLAYER).none { it.available }) {
            turnEnd()
            return MovementResult.PHRASE_CHANGE
        }
        return MovementResult.NOTHING
    }

    private val isPlayerPhrase
        get() = phrase % 2 == 0

    fun enemyMoves(): List<UnitAction> {
        val myTeam = Team.ENEMY
        val foeTeam = Team.ENEMY.opponent

        val obstacles = locationMap.toMutableMap()

        val distanceFromAlly = unitsSeq(myTeam).filterNot { it.isEmptyHanded }.associate {
            val resultMap = mutableMapOf<Position, Int>()
            calculateDistance(it, object : DistanceReceiver {
                override fun isOverMaxDistance(distance: Int): Boolean {
                    return false
                }

                override fun receive(moveStep: MoveStep): Boolean {
                    return resultMap.putIfAbsent(moveStep.position, moveStep.distanceTravel) == null
                }

            })
            it to resultMap.toMap()
        }

        val distanceToClosestEnemy = unitsSeq(myTeam).associateWith { heroUnit ->
            val distanceMap = distanceFromAlly[heroUnit] ?: return@associateWith Int.MAX_VALUE
            unitsSeq(foeTeam).mapNotNull {
                distanceMap[it.position]
            }.min() ?: Int.MAX_VALUE
        }

        val foeThreat = calculateThreat(obstacles, foeTeam).flatMap { it.second }.groupingBy { it }.eachCount()

        val lazyAllyThreat = lazy {
            calculateThreat(obstacles, myTeam).filter { (_, threatenedSeq) ->
                val threatened = threatenedSeq.toList()
                unitsSeq(foeTeam).any { threatened.contains(it.position) }
            }.map {
                it.first
            }.toSet()
        }

        val comparator = positionComparator(foeThreat)

        val possibleMoves = unitsSeq(myTeam).filter { it.available }
            .associateWith { heroUnit -> moveTargets(heroUnit).sortedWith(comparator).toList() }
        val possibleAttacks = possibleMoves.mapValues { (heroUnit, moves) ->
            autoBattleAttacks(heroUnit, moves)
        }

        val preCombatAssist = distanceToClosestEnemy.asSequence().filter { it.key.available }
            .sortedWith(compareByDescending<Map.Entry<HeroUnit, Int>> { it.value }.thenBy {
                it.key.id
            }).map {
                it.key
            }.mapNotNull {
                val assist = it.assist as? NormalAssist ?: return@mapNotNull null
                val attacks = possibleAttacks[it].orEmpty()
                val win = attacks.firstOrNull()?.winLoss == WinLoss.WIN
                if (win) {
                    null
                } else {
                    Triple(it, assist, attacks)
                }
            }.filter { (heroUnit, assist, selfAttacks) ->
                assist.isValidPreCombat(heroUnit, selfAttacks)
            }.mapNotNull { (heroUnit, assist, _) ->
                val moves = possibleMoves[heroUnit] ?: throw IllegalStateException()
                val assistTargets = moves.asSequence().flatMap { moveStep ->
                    assistTargets(heroUnit, moveStep.position).map {
                        it to moveStep
                    }
                }.distinctBy { it.first }.associate { it }
                val target =
                    assist.preCombatBestTarget(heroUnit, assistTargets.keys, lazyAllyThreat, distanceToClosestEnemy)
                if (target == null) {
                    null
                } else {
                    val moveStep = assistTargets[target] ?: throw IllegalStateException()
                    MoveAndAssist(target.id, moveStep.position, target.id)
                }
            }.firstOrNull()

        if (preCombatAssist != null) {
            executeMove(preCombatAssist)
        }


        // FIXME : fake moves
        val movements = unitsSeq(Team.ENEMY).map {
            val move = MoveOnly(
                heroUnitId = it.id,
                moveTarget = it.position
            )
            executeMove(move)
            move
        }.toList()

        turnEnd()

        return movements
    }

    private fun autoBattleAttacks(
        heroUnit: HeroUnit,
        moves: List<MoveStep>
    ): List<CombatResult> {
        return moves.asSequence().flatMap { moveStep ->
            attackTargets(heroUnit, moveStep.position).map {
                moveStep to it
            }
        }.distinctBy { it.second }.map { (movePosition, foe) ->
            val testBattle = copy()
            val heroUnitId = heroUnit.id
            val foeId = foe.id
            val (fightResult, potentialDamage) = testBattle.moveAndFight(heroUnitId, movePosition.position, foeId)
            val winLoss = when (fightResult) {
                FightResult.PLAYER_UNIT_DIED -> WinLoss.LOSS
                FightResult.PLAYER_WIN, FightResult.ENEMY_UNIT_DIED -> WinLoss.WIN
                FightResult.NOTHING -> WinLoss.DRAW
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
                heroUnit = heroUnit,
                foe = foe,
                winLoss = winLoss,
                debuffSuccess = debuffSuccess,
                potentialDamage = potentialDamage,
                damageDealt = damageDealt,
                damageReceived = damageReceived,
                cooldownChange = cooldownChange,
                cooldownChangeFoe = cooldownChangeFoe
            )
        }.sortedWith(bestAttackTarget).toList()
    }

    private fun calculateThreat(
        obstacles: MutableMap<Position, ChessPiece>,
        team: Team
    ): Sequence<Pair<HeroUnit, Sequence<Position>>> {
        return unitsSeq(team).filterNot { it.isEmptyHanded }.map { heroUnit ->
            val pass = heroUnit.skillSet.pass.any { it.apply(this, heroUnit) }
            val travelPower = heroUnit.travelPower
            val threatReceiver = if (pass) {
                ThreatWithPass(travelPower, obstacles, team)
            } else {
                ThreatWithoutPass(travelPower, obstacles)
            }
            calculateDistance(heroUnit, threatReceiver)
            heroUnit to threatReceiver.result.flatMap {
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
        }
    }

    private fun getUnit(heroUnitId: Int) =
        unitIdMap[heroUnitId] ?: throw IllegalStateException()

    private fun moveTargets(
        heroUnit: HeroUnit
    ): Sequence<MoveStep> {
        val pass = heroUnit.skillSet.pass.any { it.apply(this, heroUnit) }
        val travelPower = heroUnit.travelPower
        val obstacles = locationMap
        val team = heroUnit.team
        val distanceReceiver = DistanceReceiverRealMovement(travelPower, obstacles, team, pass)
        calculateDistance(
            heroUnit,
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
        distanceReceiver: DistanceReceiver
    ) {
        val moveType = heroUnit.moveType
        val workingMap = sortedMapOf(
            0 to sequenceOf(MoveStep(heroUnit.position, battleMap.getTerrain(heroUnit.position), false, 0))
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

    private fun assistTargets(
        heroUnit: HeroUnit,
        position: Position
    ): Sequence<HeroUnit> {
        return unitsSeq(heroUnit.team).filter { it.position.distanceTo(position) == 1 }
    }

    private fun attackTargets(
        heroUnit: HeroUnit,
        position: Position
    ): Sequence<HeroUnit> {
        if (heroUnit.isEmptyHanded) {
            return emptySequence()
        }
        val range = heroUnit.weaponType.range

        return unitsSeq(heroUnit.team.opponent).filter { it.position.distanceTo(position) == range }
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

    fun getAllPlayerMovements(): Sequence<UnitAction> {
        return unitsSeq(Team.PLAYER).filter { it.available }.flatMap { heroUnit ->
            moveTargets(heroUnit).map { it.position }.flatMap { move ->
                attackTargets(heroUnit, move).map { attackTarget ->
                    MoveAndAttack(
                        heroUnitId = heroUnit.id,
                        moveTarget = move,
                        attackTargetId = attackTarget.id
                    )
                }.plus(
                    MoveOnly(
                        heroUnitId = heroUnit.id,
                        moveTarget = move
                    )
                )
            }
        }
    }

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


data class MoveStep(
    val position: Position,
    val terrain: Terrain,
    val teleportRequired: Boolean,
    val distanceTravel: Int
) {
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

private interface DistanceReceiver {

    fun isOverMaxDistance(distance: Int): Boolean

    fun receive(moveStep: MoveStep): Boolean
}