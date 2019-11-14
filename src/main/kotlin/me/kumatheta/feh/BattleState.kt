package me.kumatheta.feh

import me.kumatheta.feh.util.compareByDescending

class BattleState private constructor(
    private val battleMap: BattleMap,
    private val locationMap: MutableMap<Position, ChessPiece>,
    phrase: Int,
    playerDied: Int,
    enemyDied: Int,
    winningTeam: Team?
) {
    var playerDied: Int = playerDied
        private set

    var enemyDied: Int = enemyDied
        private set

    var winningTeam: Team? = winningTeam
        private set

    class MovementResult(val gameEnd: Boolean, val teamLostUnit: Team?, val phraseChange: Boolean)

    private val maxX: Int = battleMap.size.x - 1
    private val maxY: Int = battleMap.size.y - 1

    var phrase = phrase
        private set
    private val unitIdMap = locationMap.values.filterIsInstance<HeroUnit>().associateBy { it.id }

    fun copy(): BattleState {
        val newLocationMap = mutableMapOf<Position, ChessPiece>()
        locationMap.mapValuesTo(newLocationMap) { it.value.copy() }
        return BattleState(
            battleMap = battleMap,
            locationMap = newLocationMap,
            phrase = phrase,
            playerDied = playerDied,
            enemyDied = enemyDied,
            winningTeam = winningTeam
        )
    }

    private fun copy(chessPieces: Sequence<ChessPiece>): BattleState {
        val newLocationMap = mutableMapOf<Position, ChessPiece>()
        chessPieces.associateByTo(newLocationMap) { it.position }
        return BattleState(
            battleMap = battleMap,
            locationMap = newLocationMap,
            phrase = phrase,
            playerDied = playerDied,
            enemyDied = enemyDied,
            winningTeam = winningTeam
        )
    }

    constructor(battleMap: BattleMap) : this(
        battleMap = battleMap,
        locationMap = battleMap.toChessPieceMap().toMutableMap(),
        phrase = 0,
        playerDied = 0,
        enemyDied = 0,
        winningTeam = null
    ) {
        startOfTurn(Team.PLAYER)
    }

    fun unitsSeq(team: Team) =
        locationMap.values.asSequence().filterIsInstance<HeroUnit>().filter { it.team == team }

    private fun executeMove(unitAction: UnitAction): Team? {
        check(winningTeam == null)
        val heroUnit = getUnit(unitAction.heroUnitId)
        check(heroUnit.available)
        move(heroUnit, unitAction.moveTarget)
        return when (unitAction) {
            is MoveOnly -> {
                standStill(heroUnit)
                null
            }
            is MoveAndAttack -> {
                val target = getUnit(unitAction.attackTargetId)
                fight(heroUnit, target).first?.team
            }
            is MoveAndAssist -> {
                val assist = heroUnit.assist ?: throw IllegalStateException()
                val target = getUnit(unitAction.assistTargetId)
                assist.apply(heroUnit, target)
                heroUnit.endOfTurn()
                null
            }
        }
    }

    private fun moveAndFight(moveAndAttack: MoveAndAttack): Pair<HeroUnit?, Int> {
        val heroUnit = getUnit(moveAndAttack.heroUnitId)
        move(heroUnit, moveAndAttack.moveTarget)
        val target = getUnit(moveAndAttack.attackTargetId)
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
            val piece = locationMap.putIfAbsent(position, heroUnit)
            require(piece == null) {
                piece.toString()
            }
            heroUnit.position = position
            val removed = locationMap.remove(originalPosition)
            check(removed == heroUnit)
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

    private class PotentialDamage(
        val attackerStat: Stat,
        val defenderStat: Stat,
        val attackOrder: List<Boolean>,
        val potentialDamage: Int
    )

    private fun preCalculateDamage(attacker: HeroUnit, defender: HeroUnit): PotentialDamage {
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

        val potentialDamage = attackOrder.asSequence().filter { it }.count() * calculateDamage(
            attacker,
            defender,
            attackerStat,
            defenderStat
        )
        return PotentialDamage(attackerStat, defenderStat, attackOrder, potentialDamage)
    }

    private fun fight(attacker: HeroUnit, defender: HeroUnit): Pair<HeroUnit?, Int> {
        check(attacker.team.foe == defender.team)
        val potentialDamage = preCalculateDamage(attacker, defender)

        val deadUnit = potentialDamage.attackOrder.asSequence().mapNotNull { attackerTurn ->
            if (attackerTurn) {
                if (singleAttack(attacker, defender, potentialDamage.attackerStat, potentialDamage.defenderStat)) {
                    defender
                } else {
                    null
                }
            } else {
                if (singleAttack(defender, attacker, potentialDamage.defenderStat, potentialDamage.attackerStat)) {
                    attacker
                } else {
                    null
                }
            }
        }.firstOrNull()

        attacker.endOfTurn()

        skillMethodApply(attacker, defender, SkillSet::postCombat)

        return Pair(deadUnit, potentialDamage.potentialDamage)
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
            if (defender.team == Team.PLAYER) {
                playerDied++
            } else {
                enemyDied++
            }
            if (unitsSeq(defender.team).none()) {
                winningTeam = defender.team.foe
            }
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
        require(isPlayerPhrase)
        val deadTeam = executeMove(unitAction)
        if (winningTeam != null) {
            return MovementResult(true, deadTeam, false)
        }
        val phraseChange = unitsSeq(Team.PLAYER).none { it.available }
        if (phraseChange) {
            turnEnd()
        }
        return MovementResult(false, deadTeam, phraseChange)
    }

    private val isPlayerPhrase
        get() = phrase % 2 == 0

    fun enemyMoves(): List<UnitAction> {
        require(!isPlayerPhrase)
        check(winningTeam == null)
        val myTeam = Team.ENEMY
        val foeTeam = Team.ENEMY.foe

        val obstacles = locationMap.toMutableMap()

        val movements = generateSequence {
            if (winningTeam != null || unitsSeq(myTeam).none { it.available }) {
                return@generateSequence null
            }
            val distanceFromAlly = distanceFrom(myTeam)
            val distanceToClosestFoe = distanceToClosestFoe(myTeam, distanceFromAlly)
            val foeThreat = calculateThreat(obstacles, foeTeam).flatMap { it.second }.groupingBy { it }.eachCount()
            val lazyAllyThreat = lazyThreat(obstacles, myTeam)

            val positionComparator = attackPositionComparator(foeThreat)

            val possibleMoves = unitsSeq(myTeam).filter { it.available }
                .associateWith { heroUnit -> moveTargets(heroUnit).sortedWith(positionComparator).toList() }
            val possibleAttacks = possibleMoves.mapValues { (heroUnit, moves) ->
                autoBattleAttacks(heroUnit, moves)
            }

            // pre combat assist
            val preCombatAssist = checkPreCombatAssist(
                distanceToClosestEnemy = distanceToClosestFoe,
                possibleAttacks = possibleAttacks,
                possibleMoves = possibleMoves,
                lazyAllyThreat = lazyAllyThreat
            )

            if (preCombatAssist != null) {
                executeMove(preCombatAssist)
                return@generateSequence preCombatAssist
            }

            // attack

            val attack = possibleAttacks.values.asSequence().mapNotNull {
                it.firstOrNull()
            }.minWith(attackerOrder)

            if (attack != null) {
                executeMove(attack.action)
                return@generateSequence attack.action
            }

            // TODO aggressive movement assist

            // TODO post combat assist

            // TODO movement
            unitsSeq(myTeam).filter { it.available }.sortedWith(movementOrder(distanceToClosestFoe)).map { heroUnit ->
                val chaseTarget = getChaseTarget(heroUnit, obstacles)
                if (chaseTarget == null) {

                } else {
                    val moves = possibleMoves[heroUnit] ?: throw IllegalStateException()
                    val distanceToTarget = distanceFrom(heroUnit, chaseTarget.position)
                    moves.asSequence().map {
                        val distanceTo = distanceToTarget[it.position] ?: throw IllegalStateException()
                        it to distanceTo
                    }.minWith(compareBy({
                        it.second
                    }, {
                        if (it.first.terrain == Terrain.DEFENSE_TILE) {
                            0
                        } else {
                            1
                        }
                    }, {
                        foeThreat[it.first.position]
                    }, {
                        if (it.first.teleportRequired) {
                            0
                        } else {
                            1
                        }
                    }, {
                        chaseTarget.position.diagonal(it.first.position)
                    }, {
                        it.first.terrain
                    }, {
                        it.first.distanceTravel
                    }, {
                        it.first.position
                    }))
                    distanceToTarget
                }
            }

            // FIXME : fake moves
            val fakeMove = unitsSeq(myTeam).filter { it.available }.map {
                MoveOnly(it.id, it.position)
            }.first()
            executeMove(fakeMove)
            fakeMove
        }.toList()

        turnEnd()
        return movements
    }

    private fun getChaseTarget(
        heroUnit: HeroUnit,
        obstacles: MutableMap<Position, ChessPiece>
    ): HeroUnit? {
        return if (heroUnit.isEmptyHanded) {
            null
        } else {
            val threadMoves = getThreatMoves(heroUnit, obstacles, false)
            val chaseTargets =
                threadMoves.moveSteps.sortedByDescending { it.distanceTravel }.flatMap { moveStep ->
                    attackPositions(heroUnit, moveStep.position).map {
                        it to moveStep
                    }
                }.associate {
                    it
                }
            unitsSeq(heroUnit.team.foe).mapNotNull { foe ->
                val moveStep = chaseTargets[foe.position]
                if (moveStep == null) {
                    null
                } else {
                    val heroUnitCopy = heroUnit.copy()
                    val foeCopy = foe.copy()
                    val testState = copy(sequenceOf(heroUnitCopy, foeCopy))
                    val chasePriority = testState.preCalculateDamage(
                        heroUnitCopy,
                        foeCopy
                    ).potentialDamage + moveStep.distanceTravel / heroUnit.travelPower
                    foe to chasePriority
                }
            }.maxWith(compareBy({
                it.second
            }, {
                it.first.id
            }))?.first
        }
    }

    private fun movementOrder(distanceToClosestFoe: Map<HeroUnit, Int>): Comparator<HeroUnit> {
        return compareBy({
            if (it.assist == null) {
                0
            } else {
                1
            }
        }, {
            when {
                it.isEmptyHanded -> 2
                it.weaponType.isRanged -> 1
                else -> 0
            }
        }, {
            distanceToClosestFoe[it]
        }, {
            it.id
        })
    }

    private fun lazyThreat(
        obstacles: MutableMap<Position, ChessPiece>,
        myTeam: Team
    ): Lazy<Set<HeroUnit>> {
        return lazy {
            calculateThreat(obstacles, myTeam).filter { (_, threatenedSeq) ->
                val threatened = threatenedSeq.toList()
                unitsSeq(myTeam.foe).any { threatened.contains(it.position) }
            }.map {
                it.first
            }.toSet()
        }
    }

    private fun distanceToClosestFoe(
        myTeam: Team,
        distanceFromAlly: Map<HeroUnit, Map<Position, Int>>
    ): Map<HeroUnit, Int> {
        return unitsSeq(myTeam).associateWith { heroUnit ->
            val distanceMap = distanceFromAlly[heroUnit] ?: return@associateWith Int.MAX_VALUE
            unitsSeq(myTeam.foe).mapNotNull {
                distanceMap[it.position]
            }.min() ?: Int.MAX_VALUE
        }
    }

    private fun distanceFrom(myTeam: Team): Map<HeroUnit, Map<Position, Int>> {
        return unitsSeq(myTeam).filterNot { it.isEmptyHanded }.associateWith {
            distanceFrom(it)
        }
    }

    private fun distanceFrom(heroUnit: HeroUnit, startingPosition: Position? = null): Map<Position, Int> {
        val resultMap = mutableMapOf<Position, Int>()
        calculateDistance(heroUnit, object : DistanceReceiver {
            override fun isOverMaxDistance(distance: Int): Boolean {
                return false
            }

            override fun receive(moveStep: MoveStep): Boolean {
                return resultMap.putIfAbsent(moveStep.position, moveStep.distanceTravel) == null
            }

        }, startingPosition)
        return resultMap.toMap()
    }

    private fun checkPreCombatAssist(
        distanceToClosestEnemy: Map<HeroUnit, Int>,
        possibleAttacks: Map<HeroUnit, List<CombatResult>>,
        possibleMoves: Map<HeroUnit, List<MoveStep>>,
        lazyAllyThreat: Lazy<Set<HeroUnit>>
    ): MoveAndAssist? {
        return distanceToClosestEnemy.asSequence().filter { it.key.available }
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
            val moveAndAttack = MoveAndAttack(heroUnitId, movePosition.position, foeId)
            val (deadUnit, potentialDamage) = testBattle.moveAndFight(moveAndAttack)
            val winLoss = when (deadUnit?.team) {
                heroUnit.team -> WinLoss.LOSS
                null -> WinLoss.DRAW
                else -> WinLoss.WIN
            }
            val testUnit = testBattle.getUnit(heroUnitId)
            val testFoe = testBattle.getUnit(foeId)
            // warning - not sure if this is correct
            val debuffSuccess = if (heroUnit.debuffer) {
                testFoe.debuff.def + testFoe.debuff.res + 2 <= foe.debuff.def + foe.debuff.res
            } else {
                false
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
                cooldownChangeFoe = cooldownChangeFoe,
                action = moveAndAttack
            )
        }.sortedWith(attackTargetOrder).toList()
    }

    private fun calculateThreat(
        obstacles: MutableMap<Position, ChessPiece>,
        team: Team
    ): Sequence<Pair<HeroUnit, Sequence<Position>>> {
        return unitsSeq(team).filterNot { it.isEmptyHanded }.map { heroUnit ->
            heroUnit to calculateThreat(heroUnit, obstacles)
        }
    }

    private fun calculateThreat(
        heroUnit: HeroUnit,
        obstacles: MutableMap<Position, ChessPiece>
    ): Sequence<Position> {
        return if (heroUnit.isEmptyHanded) {
            emptySequence()
        } else {
            val threadMoves = getThreatMoves(heroUnit, obstacles, true)
            threadMoves.movablePositions.flatMap {
                attackPositions(heroUnit, it)
            }.distinct()
        }
    }

    private fun attackPositions(
        heroUnit: HeroUnit,
        it: Position
    ): Sequence<Position> {
        val isRanged = heroUnit.weaponType.isRanged
        return if (isRanged) {
            it.surroundings.flatMap { position -> position.surroundings }
        } else {
            it.surroundings
        }
    }

    private fun getThreatMoves(
        heroUnit: HeroUnit,
        obstacles: MutableMap<Position, ChessPiece>,
        singleTurn: Boolean
    ): ThreatMoves {
        val pass = heroUnit.skillSet.pass.any { it.apply(this, heroUnit) }
        val travelPower = if (singleTurn) heroUnit.travelPower else Int.MAX_VALUE
        val threatReceiver = if (pass) {
            ThreatWithPass(travelPower, obstacles, heroUnit.team)
        } else {
            ThreatWithoutPass(travelPower, obstacles)
        }
        calculateDistance(heroUnit, threatReceiver)
        return threatReceiver
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
        calculateDistance(heroUnit, distanceReceiver)
        heroUnit.skillSet.teleport.asSequence().flatMap {
            it.apply(this, heroUnit)
        }.map {
            MoveStep(it, battleMap.getTerrain(it), true, 0)
        }.forEach {
            distanceReceiver.receive(it)
        }
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
        distanceReceiver: DistanceReceiver,
        startingPosition: Position? = null
    ) {
        val startingPositions =
            if (startingPosition == null) {
                sequenceOf(MoveStep(heroUnit.position, battleMap.getTerrain(heroUnit.position), false, 0))
            } else {
                val terrain = battleMap.getTerrain(startingPosition)
                if (terrain.moveCost(heroUnit.moveType) != null) {
                    sequenceOf(MoveStep(startingPosition, terrain, false, 0))
                } else {
                    attackPositions(heroUnit, startingPosition).mapNotNull {
                        val attackTerrain = battleMap.getTerrain(heroUnit.position)
                        if (terrain.moveCost(heroUnit.moveType) == null) {
                            null
                        } else {
                            MoveStep(it, attackTerrain, false, if (heroUnit.weaponType.isRanged) 2 else 1)
                        }
                    }
                }
            }
        calculateDistance(
            heroUnit.moveType,
            startingPositions,
            distanceReceiver
        )
    }

    private fun calculateDistance(
        moveType: MoveType,
        startingPositions: Sequence<MoveStep>,
        distanceReceiver: DistanceReceiver
    ) {
        val workingMap = sortedMapOf(
            0 to startingPositions
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

        return unitsSeq(heroUnit.team.foe).filter { it.position.distanceTo(position) == range }
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

    private fun attackPositionComparator(enemyThreat: Map<Position, Int>) = compareBy<MoveStep>(
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
            it.distanceTravel >= 0
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

private interface ThreatMoves {
    val movablePositions: Sequence<Position>
    val moveSteps: Sequence<MoveStep>
}

private abstract class ThreatReceiver(private val movementRange: Int) : DistanceReceiver, ThreatMoves {
    protected val resultMap = mutableMapOf<Position, Pair<Boolean, MoveStep>>()
    final override val movablePositions
        get() = resultMap.asSequence().filter { it.value.first }.map { it.key }
    final override val moveSteps
        get() = resultMap.asSequence().filter { it.value.first }.map { it.value.second }

    override fun isOverMaxDistance(distance: Int): Boolean {
        return distance > movementRange
    }
}

private class ThreatWithoutPass(
    movementRange: Int,
    private val obstacles: MutableMap<Position, ChessPiece>
) : ThreatReceiver(movementRange) {
    override fun receive(moveStep: MoveStep): Boolean {
        if (resultMap[moveStep.position] != null) {
            return false
        }
        val isObstacle = obstacles[moveStep.position] is Obstacle
        resultMap[moveStep.position] = Pair(isObstacle, moveStep)
        return isObstacle
    }
}

private class ThreatWithPass(
    private val movementRange: Int,
    private val obstacles: MutableMap<Position, ChessPiece>,
    private val selfTeam: Team
) : ThreatReceiver(movementRange) {

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
                resultMap[position] = Pair(false, moveStep)
                false
            }
            is HeroUnit -> {
                if (obstacle.team == selfTeam) {
                    resultMap[position] = Pair(true, moveStep)
                    true
                } else {
                    resultMap[position] = Pair(false, moveStep)
                    true
                }
            }
            null -> {
                resultMap[position] = Pair(true, moveStep)
                true
            }
        }
    }
}

private val attackTargetOrder = compareByDescending<CombatResult>(
    {
        it.winLoss
    },
    {
        if (it.debuffSuccess) {
            1
        } else {
            0
        }
    },
    {
        it.damageRatio
    },
    {
        if (it.cooldownChange > 0) {
            1
        } else {
            0
        }
    },
    {
        it.foe.id
    }
)

private val attackerOrder = compareByDescending<CombatResult>(
    {
        it.winLoss
    },
    {
        if (it.debuffSuccess) {
            1
        } else {
            0
        }
    },
    {
        if (it.heroUnit.hasSpecialDebuff) {
            1
        } else {
            0
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
            1
        } else {
            0
        }
    },
    {
        it.heroUnit.id
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