package com.bloombase.feh

class BattleState private constructor(
    private val battleMap: BattleMap,
    private val forwardMap: MutableMap<Position, ChessPiece>,
    phrase: Int
) {
    enum class MovementResult {
        WIN,
        PLAYER_UNIT_DIED,
        PHRASE_CHANGE,
        NOTHING
    }

    private val maxX: Int = battleMap.size.x - 1
    private val maxY: Int = battleMap.size.y - 1

    var phrase = phrase
        private set
    val reverseMap = mutableMapOf<HeroUnit, Position>()
    private val unitIdMap = forwardMap.values.associateBy { it.id }
    val playerUnits = reverseMap.keys.asSequence().filter { it.team == Team.PLAYER }
    private val enemyUnits = reverseMap.keys.asSequence().filter { it.team == Team.ENEMY }

    fun copyOnPlayerPhrase(): BattleState {
        check(isPlayerPhrase)
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

    private fun executeMove(unitMovement: UnitMovement): MovementResult? {
        val heroUnit = unitIdMap[unitMovement.heroUnitId] as HeroUnit
        move(heroUnit, unitMovement.move)
        return if (unitMovement.attackTargetId != null) {
            val target = unitIdMap[unitMovement.attackTargetId] as HeroUnit
            fight(heroUnit, target)
        } else {
            standStill(heroUnit)
            null
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
        if (team == Team.PLAYER)
            unitsAndPos(team.opponent).forEach {

            }
        return units
    }

    private fun units(team: Team) = reverseMap.keys.asSequence().filter { it.team == team }.toList()

    private fun fight(attacker: HeroUnit, defender: HeroUnit): MovementResult? {
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

        attackOrder.any { attackerTurn ->
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
                Team.PLAYER -> return MovementResult.PLAYER_UNIT_DIED
                Team.ENEMY -> {
                    if (enemyUnits.none()) {
                        return MovementResult.WIN
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
        return null
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
        val movementResult = executeMove(unitMovement)
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
        val threadMap = unitsAndPos(Team.PLAYER).filterNot { it.key.isEmptyHanded }.flatMap {
            moveTargets(it.key, it.value) { position, _ ->
                when (forwardMap[position]) {
                    is StationaryObject -> null
                    else -> false
                }
            }
        }.groupingBy { it.position }.eachCount()

        val comparator = positionComparator(threadMap)

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

        unitsAndPos(Team.ENEMY).filter { it.key.available }.filterNot { it.key.isEmptyHanded }.map {
            moveTargets(it.key, it.value).sortedWith(comparator)
        }
        turnEnd()
        return movements
    }

    private fun checkObstacle(position: Position, team: Team): Boolean? {
        return when (val obstacle = forwardMap[position]) {
            is StationaryObject -> null
            is HeroUnit -> if (obstacle.team == team) {
                true
            } else {
                null
            }
            null -> false
        }
    }

    fun moveTargets(heroUnit: HeroUnit): Sequence<MoveStep> {
        return moveTargets(heroUnit, reverseMap[heroUnit] ?: throw IllegalArgumentException())
    }

    private inline fun moveTargets(
        heroUnit: HeroUnit,
        position: Position,
        crossinline passThroughCheck: (Position, Team) -> Boolean? = this::checkObstacle
    ): Sequence<MoveStep> {
        val visited = mutableSetOf<Position>()
        val moveType = heroUnit.moveType

        return generateSequence(
            listOf(
                MoveStepTemp(
                    position,
                    heroUnit.travelPower,
                    false,
                    battleMap.getTerrain(position),
                    0
                )
            )
        ) {
            val next = it.asSequence().flatMap { moveStepTemp ->
                if (moveStepTemp.travelPower == 0) {
                    return@flatMap emptySequence<MoveStepTemp>()
                }
                moveStepTemp.position.surroundings.mapNotNull { position ->
                    if (!visited.add(position)) {
                        return@mapNotNull null
                    }
                    val passThroughOnly = passThroughCheck(position, heroUnit.team) ?: return@mapNotNull null
                    val terrain = battleMap.getTerrain(position)
                    val moveCost = terrain.moveCost(moveType) ?: return@mapNotNull null
                    val remaining = moveStepTemp.travelPower - moveCost
                    if (remaining < 0) {
                        return@mapNotNull null
                    } else if (remaining == 0 && passThroughOnly) {
                        return@mapNotNull null
                    }
                    MoveStepTemp(position, remaining, passThroughOnly, terrain, moveStepTemp.distanceTravel + 1)
                }
            }.toList()
            if (next.isEmpty()) {
                null
            } else {
                next
            }
        }.flatMap {
            it.asSequence()
        }.mapNotNull {
            it.toMoveStep()
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

    private fun positionComparator(threadMap: Map<Position, Int>) = compareBy<MoveStep>(
        {
            if (it.terrain == Terrain.DEFENSE_TILE) {
                0
            } else {
                1
            }
        },
        {
            threadMap[it.position] ?: 0
        },
        {
            it.terrain
        }
    )
}

class MoveStep(val position: Position, val terrain: Terrain, val teleportRequired: Boolean, val distanceTravel: Int)

private class MoveStepTemp(
    val position: Position,
    val travelPower: Int,
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