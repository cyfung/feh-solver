package me.kumatheta.feh

import me.kumatheta.feh.skill.assist.Pivot
import me.kumatheta.feh.util.attackPositionOrder
import me.kumatheta.feh.util.attackTargetOrder
import me.kumatheta.feh.util.attackTargetPositions
import me.kumatheta.feh.util.attackerOrder
import me.kumatheta.feh.util.bodyBlockTargetOrder
import me.kumatheta.feh.util.moveTargetOrder
import me.kumatheta.feh.util.surroundings
import me.kumatheta.feh.util.unitMoveOrder

private val protectiveAssistPositionOrder = compareBy<Triple<HeroUnit, MoveStep, Int>>({
    it.third
}, {
    if (it.second.teleportRequired) 0 else 1
}, {
    it.second.distanceTravel
}, {
    it.second.position
})

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

    val maxPosition = Position(battleMap.size.x - 1, battleMap.size.y - 1)

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

    fun isValidPosition(heroUnit: HeroUnit, position: Position): Boolean {
        if (position.x < 0 || position.y < 0 || position.x > maxPosition.x || position.y >= maxPosition.y) {
            return false
        }
        return battleMap.getTerrain(position).moveCost(heroUnit.moveType) != null
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
                heroUnit.endOfTurn()
                null
            }
            is MoveAndAttack -> {
                val target = getUnit(unitAction.attackTargetId)
                fight(heroUnit, target).first?.team
            }
            is MoveAndBreak -> {
                val obstacle = locationMap[unitAction.obstacle] as Obstacle
                if (--obstacle.health == 0) {
                    locationMap.remove(unitAction.obstacle)
                }
                heroUnit.endOfTurn()
                null
            }
            is MoveAndAssist -> {
                val assist = heroUnit.assist ?: throw IllegalStateException()
                val target = getUnit(unitAction.assistTargetId)
                assist.apply(heroUnit, target, this)
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

    internal fun move(heroUnit: HeroUnit, position: Position) {
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
        val attackerInCombat: InCombatStatus,
        val defenderInCombat: InCombatStatus,
        val attackOrder: List<Boolean>,
        val potentialDamage: Int
    )

    private fun preCalculateDamage(
        attacker: HeroUnit,
        defender: HeroUnit
    ): PotentialDamage {
        val attackerSkills = InCombatSkillSet(
            defender.skillSet.skills.asSequence() + attacker.skillSet.foeEffect.attackerSkillsSeq(
                attacker,
                defender
            ).filterNotNull()
        )
        val defenderSkills = InCombatSkillSet(
            defender.skillSet.skills.asSequence() + attacker.skillSet.foeEffect.attackerSkillsSeq(
                attacker,
                defender
            ).filterNotNull()
        )
        val attackerStat = attacker.stat + attacker.buff + attacker.debuff +
                attackerSkills.inCombatStat.mapAttackerSkills(attacker, defender).fold(Stat.ZERO) { acc, stat ->
                    acc + stat
                }
        val defenderStat = defender.stat + defender.buff + defender.debuff +
                defenderSkills.inCombatStat.mapDefenderSkills(attacker, defender).fold(Stat.ZERO) { acc, stat ->
                    acc + stat
                }

        val spdDiff = attackerStat.spd - defenderStat.spd

        val attackerInCombat = InCombatStatus(attacker, attackerStat, attackerSkills)
        val defenderInCombat = InCombatStatus(attacker, defenderStat, defenderSkills)

        val attackOrder = createAttackOrder(attackerInCombat, attackerInCombat, spdDiff)

        val potentialDamage = attackOrder.asSequence().filter { it }.count() * calculateDamage(
            attackerInCombat,
            defenderInCombat
        )
        return PotentialDamage(attackerInCombat, defenderInCombat, attackOrder, potentialDamage)
    }

    private fun fight(attacker: HeroUnit, defender: HeroUnit): Pair<HeroUnit?, Int> {
        check(attacker.team.foe == defender.team)
        val potentialDamage = preCalculateDamage(attacker, defender)

        var attackerAttacked = false
        var defenderAttacked = false
        val deadUnit = potentialDamage.attackOrder.asSequence().mapNotNull { attackerTurn ->
            if (attackerTurn) {
                attackerAttacked = true
                if (singleAttack(potentialDamage.attackerInCombat, potentialDamage.defenderInCombat)) {
                    defender
                } else {
                    null
                }
            } else {
                defenderAttacked = true
                if (singleAttack(potentialDamage.defenderInCombat, potentialDamage.attackerInCombat)) {
                    attacker
                } else {
                    null
                }
            }
        }.firstOrNull()

        attacker.endOfTurn()

        potentialDamage.attackerInCombat.skills.postCombat.forEach {
            it.apply(
                this,
                attacker,
                defender,
                true,
                attackerAttacked
            )
        }
        potentialDamage.defenderInCombat.skills.postCombat.forEach {
            it.apply(
                this,
                defender,
                attacker,
                false,
                defenderAttacked
            )
        }

        locationMap.values.asSequence().filterIsInstance<HeroUnit>().forEach {
            it.endOfCombat()
        }

        if (!attacker.engaged) {
            setGroupEngaged(attacker)
        }

        if (!defender.engaged) {
            setGroupEngaged(defender)
        }

        return Pair(deadUnit, potentialDamage.potentialDamage)
    }

    private fun setGroupEngaged(heroUnit: HeroUnit) {
        val group = heroUnit.group ?: return
        unitsSeq(heroUnit.team).filter {
            it.group == group
        }.forEach {
            it.setEngaged(it == heroUnit)
        }
    }

    private fun singleAttack(
        damageDealer: InCombatStatus,
        damageReceiver: InCombatStatus
    ): Boolean {
        val dead = damageReceiver.heroUnit.takeDamage(calculateDamage(damageDealer, damageReceiver))
        val attackerCooldownIncrease =
            damageDealer.skills.cooldownBuff.asSequence().map {
                it.apply(
                    this,
                    damageDealer,
                    damageReceiver,
                    true
                )
            }.max() ?: 0
        val attackerCooldownReduce =
            damageReceiver.skills.cooldownDebuff.asSequence().map {
                it.apply(
                    this,
                    damageReceiver,
                    damageDealer,
                    false
                )
            }.max() ?: 0

        damageDealer.heroUnit.reduceCooldown(1 + attackerCooldownIncrease - attackerCooldownReduce)
        if (!dead) {
            val defenderCooldownIncrease =
                damageDealer.skills.cooldownBuff.asSequence().map {
                    it.apply(
                        this,
                        damageReceiver,
                        damageDealer,
                        false
                    )
                }.max() ?: 0
            val defenderCooldownReduce =
                damageReceiver.skills.cooldownDebuff.asSequence().map {
                    it.apply(
                        this,
                        damageDealer,
                        damageReceiver,
                        true
                    )
                }.max()
                    ?: 0
            damageReceiver.heroUnit.reduceCooldown(1 + defenderCooldownIncrease - defenderCooldownReduce)
        } else {
            locationMap.remove(damageReceiver.heroUnit.position)
            val teamDied = damageReceiver.heroUnit.team
            if (teamDied == Team.PLAYER) {
                playerDied++
            } else {
                enemyDied++
            }
            if (unitsSeq(teamDied).none()) {
                winningTeam = teamDied.foe
            }
        }
        return dead
    }

    private fun createAttackOrder(
        attackerInCombat: InCombatStatus,
        defenderInCombat: InCombatStatus,
        spdDiff: Int
    ): MutableList<Boolean> {
        val attacker = attackerInCombat.heroUnit
        val defender = defenderInCombat.heroUnit
        val attackerSkillSet = attackerInCombat.skills
        val defenderSkillSet = defenderInCombat.skills

        val rangeMatch = when {
            attacker.isEmptyHanded -> false
            attacker.weaponType.isRanged == defender.weaponType.isRanged -> true
            else -> {
                defenderSkillSet.counterIgnoreRange.mapDefenderSkills(attackerInCombat, defenderInCombat).any()
            }
        }

        val canCounter = rangeMatch

        val disablePriorityChange =
            attackerSkillSet.disablePriorityChange.mapAttackerSkills(attackerInCombat, defenderInCombat).any() ||
                    defenderSkillSet.disablePriorityChange.mapDefenderSkills(attackerInCombat, defenderInCombat).any()

        val desperation: Boolean
        val vantage: Boolean
        if (disablePriorityChange) {
            desperation = false
            vantage = false
        } else {
            desperation = attackerSkillSet.desperation.mapAttackerSkills(attackerInCombat, defenderInCombat).any()
            vantage = defenderSkillSet.vantage.mapDefenderSkills(attackerInCombat, defenderInCombat).any()
        }

        val attackerFollowup = when (val guarantee =
            attackerSkillSet.followUp.mapAttackerSkills(attackerInCombat, defenderInCombat).sum()) {
            0 -> spdDiff >= 5
            else -> guarantee > 0
        }
        val defenderFollowup = when (val guarantee =
            defenderSkillSet.followUp.mapDefenderSkills(attackerInCombat, defenderInCombat).sum()) {
            0 -> spdDiff <= -5
            else -> guarantee > 0
        }

        val attackOrder = mutableListOf<Boolean>()
        val attackerBrave = attackerSkillSet.brave.mapAttackerSkills(attackerInCombat, defenderInCombat).any()
        val defenderBrave = defenderSkillSet.brave.mapDefenderSkills(attackerInCombat, defenderInCombat).any()
        val addAttacker = {
            attackOrder.add(true)
            if (attackerBrave) {
                attackOrder.add(true)
            }
        }
        val addDefender = {
            attackOrder.add(false)
            if (defenderBrave) {
                attackOrder.add(false)
            }
        }

        // actual add attack orders
        if (vantage) {
            if (canCounter) {
                addDefender()
            }
        }
        addAttacker()

        if (attackerFollowup && desperation) {
            addAttacker()
        }

        // normal counter
        if (!vantage) {
            if (canCounter) {
                addDefender()
            }
        }

        // vantage followup counter
        if (defenderFollowup && vantage) {
            if (canCounter) {
                addDefender()
            }
        }

        if (attackerFollowup && !desperation) {
            addAttacker()
        }

        // normal followup counter
        if (defenderFollowup && !vantage) {
            if (canCounter) {
                addDefender()
            }
        }
        return attackOrder
    }

    private fun calculateDamage(
        damageDealer: InCombatStatus,
        damageReceiver: InCombatStatus
    ): Int {
        val targetRes = damageDealer.heroUnit.weaponType.targetRes
        val defenderDefRes = if (targetRes) {
            damageReceiver.inCombatStat.res
        } else {
            damageReceiver.inCombatStat.def
        }
        val effAtk = if (isEffective(damageDealer.heroUnit, damageReceiver.heroUnit)) {
            damageDealer.inCombatStat.atk * 3 / 2
        } else {
            damageDealer.inCombatStat.atk
        }
        val colorAdvantage = damageDealer.heroUnit.getColorAdvantage(damageReceiver.heroUnit)
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
        return attacker.isEffective(defender)
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

        var obstacles = locationMap.toMap()

        val movements = generateSequence {
            if (winningTeam != null || unitsSeq(myTeam).none { it.available }) {
                return@generateSequence null
            }
            val distanceFromAlly = distanceFrom(myTeam)
            val distanceToClosestFoe = distanceToClosestFoe(myTeam, distanceFromAlly)
            val foeThreat = calculateThreat(foeTeam, obstacles).flatMap { it.second }.groupingBy { it }.eachCount()
            val lazyAllyThreat = lazyThreat(obstacles, myTeam)

            val availableUnits = unitsSeq(myTeam).filter { it.available }.toList()
            val possibleMoves = availableUnits.asSequence().associateWith { heroUnit ->
                moveTargets(heroUnit).sortedWith(attackPositionOrder(heroUnit, foeThreat))
                    .associateBy { it.position }
            }

            val attackTargetPositions = possibleMoves.mapValues { (heroUnit, moves) ->
                moves.values.asSequence().flatMap { moveStep ->
                    attackTargetPositions(heroUnit, moveStep.position, maxPosition).mapNotNull {
                        it to moveStep
                    }
                }.distinctBy { it.second }.associate { it }
            }

            val possibleAttacks = attackTargetPositions.mapValues { (heroUnit, targetPositions) ->
                autoBattleAttacks(heroUnit, targetPositions)
            }

            val assistSortedAllies = distanceToClosestFoe.asSequence().filter {
                it.key.available
            }.toSortedSet(compareBy({
                if (it.key.isEmptyHanded) {
                    0
                } else {
                    1
                }
            }, {
                // distance to closest enemy, highest first
                -it.value
            }, {
                it.key.id
            })).map {
                it.key
            }

            val allyAssistTargets = possibleMoves.mapValues { (heroUnit, moves) ->
                lazy {
                    heroUnit.assistTargets(moves).distinctBy { it.first }.associate { it }
                }
            }

            // pre combat assist
            val preCombatAssist = assistSortedAllies.asSequence().checkPreCombatAssist(
                distanceToClosestFoe = distanceToClosestFoe,
                possibleAttacks = possibleAttacks,
                allyAssistTargets = allyAssistTargets,
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
                val foeOriginalPosition = attack.foe.position
                val deadTeam = executeMove(attack.action)
                if (deadTeam == foeTeam || foeOriginalPosition != attack.foe.position) {
                    obstacles = locationMap.toMap()
                }
                return@generateSequence attack.action
            }

            // post combat assist
            val postCombatAssist = assistSortedAllies.asSequence().mapNotNull { heroUnit ->
                val assist = heroUnit.assist as? NormalAssist ?: return@mapNotNull null
                val assistTargets = allyAssistTargets[heroUnit]?.value ?: throw IllegalStateException()
                val target = assist.postCombatBestTarget(
                    self = heroUnit,
                    targets = assistTargets.keys,
                    lazyAllyThreat = lazyAllyThreat,
                    foeThreat = foeThreat,
                    distanceToClosestFoe = distanceToClosestFoe,
                    battleState = this
                )
                if (target == null) {
                    null
                } else {
                    val moveStep = assistTargets[target] ?: throw IllegalStateException()
                    MoveAndAssist(heroUnit.id, moveStep.position, target.id)
                }
            }.firstOrNull()

            if (postCombatAssist != null) {
                executeMove(postCombatAssist)
                return@generateSequence postCombatAssist
            }

            // aggressive movement assist
            val allyThreat = lazyAllyThreat.value

            val aggressiveAssist = getAggressiveAssist(assistSortedAllies, possibleMoves, foeThreat, allyThreat)

            if (aggressiveAssist != null) {
                executeMove(aggressiveAssist)
                return@generateSequence aggressiveAssist
            }

            // movement assist

            // for calculating body blocking
            val lazyFoeMeleeMoves = lazy {
                val obstaclesOnly = locationMap.filterValues { it is Obstacle }
                unitsSeq(foeTeam).filterNot {
                    it.isEmptyHanded
                }.filterNot {
                    it.weaponType.isRanged
                }.flatMap { enemy ->
                    moveTargets(enemy, obstaclesOnly)
                }.map {
                    it.position
                }
            }

            val movementAssist =
                getProtectiveMovementAssist(assistSortedAllies, possibleMoves, lazyFoeMeleeMoves, foeThreat)
            if (movementAssist != null) {
                executeMove(movementAssist)
                return@generateSequence movementAssist
            }

            // movement
            val move = getMoveAction(
                availableUnits,
                distanceToClosestFoe,
                possibleMoves,
                attackTargetPositions,
                distanceFromAlly,
                foeThreat
            )
            if (move != null) {
                executeMove(move)
                return@generateSequence move
            }

            // no action, cleanup
            availableUnits.asSequence().forEach {
                it.endOfTurn()
            }
            null
        }.toList()

        turnEnd()
        return movements
    }

    private fun getProtectiveMovementAssist(
        assistSortedAllies: List<HeroUnit>,
        possibleMoves: Map<HeroUnit, Map<Position, MoveStep>>,
        lazyFoeMeleeMoves: Lazy<Sequence<Position>>,
        foeThreat: Map<Position, Int>
    ): UnitAction? {
        // for body blocking
        // important: only valid to calculate like this because
        // 1. the max movement is 3 tiles
        // 2. we only care about melee enemy
        // 3. block blocker is with melee weapon
        // 4. block blocker is required to be adjacent to the assist target
        // 5. block blocker cannot attack enemy
        // otherwise there are cases where body blocker can block a space and effectively blocking 2 spaces which the enemies can attack.
        return assistSortedAllies.asSequence().mapNotNull { heroUnit ->
            val assist = heroUnit.assist as? ProtectiveMovementAssist ?: return@mapNotNull null
            val moves = possibleMoves[heroUnit] ?: throw IllegalStateException()
            val assistTargets = heroUnit.assistTargets(moves)
            if (!heroUnit.isEmptyHanded && !heroUnit.weaponType.isRanged) {
                val foeMeleeMoves = lazyFoeMeleeMoves.value
                val bodyBlock = assistTargets.asSequence().filter { (_, moveStep) ->
                    foeMeleeMoves.contains(moveStep.position)
                }.filterNot { (heroUnit, moveStep) ->
                    heroUnit.position.surroundings(maxPosition).filterNot { it == moveStep.position }.any {
                        foeMeleeMoves.contains(it)
                    }
                }.minWith(compareBy(bodyBlockTargetOrder) { it.first })
                if (bodyBlock != null) {
                    return@mapNotNull MoveOnly(heroUnit.id, bodyBlock.second.position)
                }
            }
            val assistTarget = assistTargets.asSequence().mapNotNull validTarget@{ (target, moveStep) ->
                val originalThreat = foeThreat[target.position] ?: 0
                if (originalThreat == 0) return@validTarget null
                val targetEndPosition = assist.targetEndPosition(this, heroUnit, moveStep.position, target.position)
                val newThreat = foeThreat[targetEndPosition] ?: 0
                if (newThreat >= originalThreat) return@validTarget null
                val selfNewThreat = foeThreat[assist.selfEndPosition(moveStep.position, target.position)] ?: 0
                Triple(target, moveStep, newThreat + selfNewThreat)
            }.groupBy { it.first }.mapValues { (_, choices) ->
                choices.minWith(
                    protectiveAssistPositionOrder
                )?.second ?: throw IllegalStateException()
            }.minWith(compareBy({
                if (it.value.teleportRequired) {
                    0
                } else {
                    1
                }
            }, {
                it.value.distanceTravel
            }, {
                it.key.visibleStat.totalExceptHp
            }, {
                it.key.position
            }))
            if (assistTarget == null) {
                null
            } else {
                MoveAndAssist(heroUnit.id, assistTarget.value.position, assistTarget.key.id)
            }
        }.firstOrNull()
    }

    private fun getAggressiveAssist(
        assistSortedAllies: List<HeroUnit>,
        possibleMoves: Map<HeroUnit, Map<Position, MoveStep>>,
        foeThreat: Map<Position, Int>,
        allyThreat: Map<HeroUnit, Set<HeroUnit>>
    ): MoveAndAssist? {
        return assistSortedAllies.asSequence().mapNotNull { heroUnit ->
            val assist = heroUnit.assist as? MovementAssist ?: return@mapNotNull null
            if (!assist.canBeAggressive) return@mapNotNull null
            val moves = possibleMoves[heroUnit] ?: throw IllegalStateException()
            val assistTargets =
                heroUnit.assistTargets(moves).groupBy({ it.first }, { it.second }).mapValues { (target, moveSteps) ->
                    moveSteps.minWith(compareBy<MoveStep> {
                        foeThreat[assist.selfEndPosition(it.position, target.position)] ?: 0
                    }.then(compareBy(
                        {
                            if (it.teleportRequired) 0 else 1
                        },
                        {
                            it.distanceTravel
                        },
                        {
                            it.position
                        }
                    ))) ?: throw IllegalStateException()
                }

            if (assistTargets.isEmpty()) return@mapNotNull null

            val (_, target, moveStep) = getAggressiveAssist(allyThreat, heroUnit, assistTargets, false)
                ?: getAggressiveAssist(allyThreat, heroUnit, assistTargets, true) ?: return@mapNotNull null

            MoveAndAssist(heroUnit.id, moveStep.position, target.id)
        }.firstOrNull()
    }

    private fun getAggressiveAssist(
        allyThreat: Map<HeroUnit, Set<HeroUnit>>,
        heroUnit: HeroUnit,
        assistTargets: Map<HeroUnit, MoveStep>,
        isRanged: Boolean
    ): Triple<HeroUnit, HeroUnit, MoveStep>? {
        val range = if (isRanged) 2 else 1
        return allyThreat.asSequence().filter { it.key.available }.filterNot { it.key == heroUnit }
            .filter { it.key.weaponType.isRanged == isRanged }.flatMap { it.value.asSequence() }.distinct()
            .flatMap { enemy ->
                assistTargets.asSequence().filter { it.key.position.distanceTo(enemy.position) == range }.map {
                    Triple(enemy, it.key, it.value)
                }
            }.minWith(compareBy({
                if (it.third.teleportRequired) {
                    0
                } else {
                    1
                }
            }, {
                it.third.distanceTravel
            }, {
                -it.first.id
            }, {
                it.second.position
            }))
    }

    private fun getMoveAction(
        availableUnits: List<HeroUnit>,
        distanceToClosestFoe: Map<HeroUnit, Int>,
        possibleMoves: Map<HeroUnit, Map<Position, MoveStep>>,
        attackTargetPositions: Map<HeroUnit, Map<Position, MoveStep>>,
        distanceFromAlly: Map<HeroUnit, Map<Position, Int>>,
        foeThreat: Map<Position, Int>
    ): UnitAction? {
        return availableUnits.asSequence().filter { it.engaged }.sortedWith(unitMoveOrder(distanceToClosestFoe))
            .mapNotNull { heroUnit ->
                val moves = possibleMoves[heroUnit] ?: throw IllegalStateException()
                val distanceMap = distanceFromAlly[heroUnit] ?: throw IllegalStateException()
                val targetPositions = attackTargetPositions[heroUnit] ?: throw IllegalStateException()
                getMoveAction(heroUnit, distanceMap, moves, targetPositions, foeThreat)
            }.firstOrNull()
    }

    private fun getClosestAlly(
        heroUnit: HeroUnit,
        distanceMap: Map<Position, Int>
    ): HeroUnit? {
        val distanceTo = teammates(heroUnit).associateWith {
            distanceMap[it.position]
        }
        if (distanceTo.values.any { it == null }) {
            return null
        }
        return distanceTo.asSequence().minWith(compareBy<Map.Entry<HeroUnit, Int?>> {
            it.value
        }.thenByDescending {
            it.key.id
        })?.key
    }

    private fun getMoveAction(
        heroUnit: HeroUnit,
        distanceMap: Map<Position, Int>,
        moves: Map<Position, MoveStep>,
        attackTargetPositions: Map<Position, MoveStep>,
        foeThreat: Map<Position, Int>
    ): UnitAction? {
        val chaseTarget = getChaseTarget(heroUnit, distanceMap) ?: getClosestAlly(heroUnit, distanceMap) ?: return null

        val distanceToTarget = distanceFrom(heroUnit, chaseTarget.position)
        val okToStay = if (chaseTarget.team == heroUnit.team) {
            unitsSeq(heroUnit.team.foe).mapNotNull { it.position.distanceTo(heroUnit.position) }.all { it > 2 }
        } else {
            false
        }

        val basicMove = moves.values.asSequence().let { seq ->
            if (okToStay) {
                seq
            } else {
                seq.filterNot { it.position == heroUnit.position }
            }
        }.minWith(moveTargetOrder(heroUnit, foeThreat, chaseTarget, distanceToTarget))?.position ?: heroUnit.position

        val basicDistance = basicMove.distanceTo(chaseTarget.position)

        val validObstacles = locationMap.values.asSequence().filterIsInstance<Obstacle>().mapNotNull {
            val distance = it.position.distanceTo(chaseTarget.position)
            if (distance < basicDistance) {
                it.position to distance
            } else {
                null
            }
        }.associate { it }

        val obstacle = if (validObstacles.isEmpty()) {
            null
        } else {
            attackTargetPositions.asSequence().mapNotNull { (position, moveStep) ->
                val distance = validObstacles[position] ?: return@mapNotNull null
                Triple(distance, position, moveStep)
            }.minBy {
                it.first
            }
        }

        if (obstacle != null) {
            return MoveAndBreak(heroUnit.id, obstacle.third.position, obstacle.second)
        }

        if (heroUnit.assist == Pivot) {
            val pivot = heroUnit.assistTargets(moves).mapNotNull {
                val endPosition = Pivot.selfEndPosition(it.second.position, it.first.position)
                val distanceTo = endPosition.distanceTo(chaseTarget.position)
                if (distanceTo < basicDistance) {
                    Triple(distanceTo, it.first, it.second)
                } else {
                    null
                }
            }.minWith(compareBy({
                it.first
            }, {
                it.second.id
            }, {
                it.third.position
            }))

            if (pivot != null) {
                return MoveAndAssist(heroUnit.id, pivot.third.position, pivot.second.id)
            }
        }

        if (basicMove != heroUnit.position) {
            // TODO Rally (1 point increase)
            return MoveOnly(heroUnit.id, basicMove)
        }

        return null
    }

    private fun lazyThreat(
        obstacles: Map<Position, ChessPiece>,
        myTeam: Team
    ): Lazy<Map<HeroUnit, Set<HeroUnit>>> {
        return lazy {
            val enemies = unitsSeq(myTeam.foe).associateBy { it.position }
            calculateThreat(myTeam, obstacles).mapNotNull { (heroUnit, threatenedSeq) ->
                val threatenedEnemies = threatenedSeq.mapNotNull { enemies[it] }.toSet()
                if (threatenedEnemies.isEmpty()) {
                    null
                } else {
                    heroUnit to threatenedEnemies
                }
            }.associate { it }
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

    private fun Sequence<HeroUnit>.checkPreCombatAssist(
        distanceToClosestFoe: Map<HeroUnit, Int>,
        possibleAttacks: Map<HeroUnit, List<CombatResult>>,
        allyAssistTargets: Map<HeroUnit, Lazy<Map<HeroUnit, MoveStep>>>,
        lazyAllyThreat: Lazy<Map<HeroUnit, Set<HeroUnit>>>
    ): MoveAndAssist? {
        return mapNotNull { heroUnit ->
            val assist = heroUnit.assist as? NormalAssist ?: return@mapNotNull null
            val attacks = possibleAttacks[heroUnit].orEmpty()
            val win = attacks.firstOrNull()?.winLoss == WinLoss.WIN
            if (win || !assist.isValidPreCombat(heroUnit, attacks)) {
                return@mapNotNull null
            }
            val assistTargets = allyAssistTargets[heroUnit]?.value ?: throw IllegalStateException()
            val target =
                assist.preCombatBestTarget(heroUnit, assistTargets.keys, lazyAllyThreat, distanceToClosestFoe)
            if (target == null) {
                null
            } else {
                val moveStep = assistTargets[target] ?: throw IllegalStateException()
                MoveAndAssist(heroUnit.id, moveStep.position, target.id)
            }
        }.firstOrNull()
    }

    private fun HeroUnit.assistTargets(
        moves: Map<Position, MoveStep>
    ): Sequence<Pair<HeroUnit, MoveStep>> {
        if (assist == null) return emptySequence()
        val teammates = teammates(this).toList()
        return moves.values.asSequence().flatMap { moveStep ->
            teammates.asSequence().filterValidAssistTarget(this, moveStep.position).map {
                it to moveStep
            }
        }
    }

    private fun autoBattleAttacks(
        heroUnit: HeroUnit,
        attackTargetPositions: Map<Position, MoveStep>
    ): List<CombatResult> {
        return unitsSeq(heroUnit.team.foe).asSequence().mapNotNull {
            val moveStep = attackTargetPositions[it.position]
            if (moveStep == null) {
                null
            } else {
                moveStep to it
            }
        }.map { (movePosition, foe) ->
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
            val cooldownChange = (testUnit.cooldown ?: 0) - (heroUnit.cooldown ?: 0)
            val cooldownChangeFoe = (testFoe.cooldown ?: 0) - (foe.cooldown ?: 0)
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
        team: Team,
        obstacles: Map<Position, ChessPiece>
    ): Sequence<Pair<HeroUnit, Sequence<Position>>> {
        return unitsSeq(team).filterNot { it.isEmptyHanded }.map { heroUnit ->
            heroUnit to calculateThreat(heroUnit, obstacles)
        }
    }

    private fun calculateThreat(
        heroUnit: HeroUnit,
        obstacles: Map<Position, ChessPiece>
    ): Sequence<Position> {
        return if (heroUnit.isEmptyHanded) {
            emptySequence()
        } else {
            val pass = heroUnit.skillSet.pass.any { it.apply(this, heroUnit) }
            val travelPower = heroUnit.travelPower
            val threatReceiver = if (pass) {
                ThreatWithPass(travelPower, obstacles, heroUnit.team)
            } else {
                ThreatWithoutPass(travelPower, obstacles)
            }
            calculateDistance(heroUnit, threatReceiver)
            threatReceiver.movablePositions.flatMap {
                attackTargetPositions(heroUnit, it, maxPosition)
            }.distinct()
        }
    }

    fun getChessPiece(position: Position) = locationMap[position]

    private fun getUnit(heroUnitId: Int) =
        unitIdMap[heroUnitId] ?: throw IllegalStateException()

    private fun moveTargets(
        heroUnit: HeroUnit,
        obstacles: Map<Position, ChessPiece> = locationMap
    ): Sequence<MoveStep> {
        val pass = heroUnit.skillSet.pass.any { it.apply(this, heroUnit) }
        val travelPower = heroUnit.travelPower
        val distanceReceiver = DistanceReceiverRealMovement(travelPower, obstacles, heroUnit, pass)
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
                0 to sequenceOf(MoveStep(heroUnit.position, battleMap.getTerrain(heroUnit.position), false, 0))
            } else {
                val terrain = battleMap.getTerrain(startingPosition)
                if (terrain.moveCost(heroUnit.moveType) != null) {
                    0 to sequenceOf(MoveStep(startingPosition, terrain, false, 0))
                } else {
                    val distanceTravel = if (heroUnit.weaponType.isRanged) 2 else 1
                    distanceTravel to attackTargetPositions(heroUnit, startingPosition, maxPosition).mapNotNull {
                        val attackTerrain = battleMap.getTerrain(heroUnit.position)
                        if (attackTerrain.moveCost(heroUnit.moveType) == null) {
                            null
                        } else {
                            MoveStep(it, attackTerrain, false, distanceTravel)
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
        startingPositions: Pair<Int, Sequence<MoveStep>>,
        distanceReceiver: DistanceReceiver
    ) {
        val workingMap = sortedMapOf(
            startingPositions
        )

        while (workingMap.isNotEmpty()) {
            val currentDistance = workingMap.firstKey()
            if (distanceReceiver.isOverMaxDistance(currentDistance)) {
                break
            }
            val temp = workingMap.remove(currentDistance) ?: throw IllegalStateException()
            temp.asSequence().filter {
                distanceReceiver.receive(it)
            }.flatMap { it.position.surroundings(maxPosition) }.mapNotNull { position ->
                val terrain = battleMap.getTerrain(position)
                val moveCost = terrain.moveCost(moveType) ?: return@mapNotNull null
                val distanceTravel = currentDistance + moveCost
                distanceTravel to MoveStep(position, terrain, false, distanceTravel)
            }.groupBy({ it.first }, { it.second }).forEach { (distance, list) ->
                workingMap.add(distance, list.asSequence())
            }
        }
    }

    private fun Sequence<HeroUnit>.filterValidAssistTarget(
        heroUnit: HeroUnit,
        position: Position
    ): Sequence<HeroUnit> {
        val assist = heroUnit.assist ?: return emptySequence()
        val assistDistance = if (assist.isRange) {
            2
        } else {
            1
        }
        return filter { it.position.distanceTo(position) == assistDistance }.filter { target ->
            assist.isValidAction(heroUnit, target, this@BattleState, position)
        }
    }

    private fun <T, U> Sequence<CombatSkill<T, U>>.mapAttackerSkills(
        attacker: U,
        defender: U
    ) = map { it.apply(this@BattleState, attacker, defender, true) }

    private fun <T, U> Sequence<CombatSkill<T, U>>.mapDefenderSkills(
        attacker: U,
        defender: U
    ) = map { it.apply(this@BattleState, defender, attacker, false) }

    private fun <T, U> List<CombatSkill<T, U>>.attackerSkillsSeq(
        attacker: U,
        defender: U
    ) = asSequence().mapAttackerSkills(attacker, defender)

    private fun <T, U> List<CombatSkill<T, U>>.defenderSkillsSeq(
        attacker: U,
        defender: U
    ) = asSequence().mapDefenderSkills(attacker, defender)

    fun getAllPlayerMovements(): Sequence<UnitAction> {
        return unitsSeq(Team.PLAYER).filter { it.available }.flatMap { heroUnit ->
            val teammates = teammates(heroUnit).toList()
            moveTargets(heroUnit).map { it.position }.flatMap { move ->
                attackTargetPositions(heroUnit, move, maxPosition).mapNotNull { attackTargetPosition ->
                    when (val chessPiece = locationMap[attackTargetPosition]) {
                        null -> null
                        is HeroUnit -> if (chessPiece.team == Team.ENEMY) {
                            MoveAndAttack(
                                heroUnitId = heroUnit.id,
                                moveTarget = move,
                                attackTargetId = chessPiece.id
                            )
                        } else {
                            null
                        }
                        is Obstacle -> MoveAndBreak(
                            heroUnitId = heroUnit.id,
                            moveTarget = move,
                            obstacle = attackTargetPosition
                        )
                    }
                } + MoveOnly(
                    heroUnitId = heroUnit.id,
                    moveTarget = move
                ) + teammates.asSequence().filterValidAssistTarget(heroUnit, move).map { assistTarget ->
                    MoveAndAssist(
                        heroUnitId = heroUnit.id,
                        moveTarget = move,
                        assistTargetId = assistTarget.id
                    )
                }
            }
        }
    }

    private fun getChaseTarget(
        heroUnit: HeroUnit,
        distanceTo: Map<Position, Int>
    ): HeroUnit? {
        return if (heroUnit.isEmptyHanded) {
            null
        } else {
            val chaseTargets = distanceTo.asSequence().flatMap { (position, distance) ->
                attackTargetPositions(heroUnit, position, maxPosition).map {
                    it to distance
                }
            }.groupingBy {
                it.first
            }.aggregate { _, accumulator: Int?, element, _ ->
                accumulator ?: element.second
            }
            unitsSeq(heroUnit.team.foe).mapNotNull { foe ->
                val distance = chaseTargets[foe.position]
                if (distance == null) {
                    null
                } else {
                    val heroUnitCopy = heroUnit.copy()
                    val foeCopy = foe.copy()
                    val testState = copy(sequenceOf(heroUnitCopy, foeCopy))
                    val chasePriority = testState.preCalculateDamage(
                        heroUnitCopy,
                        foeCopy
                    ).potentialDamage + distance / heroUnit.travelPower
                    foe to chasePriority
                }
            }.maxWith(compareBy({
                it.second
            }, {
                it.first.id
            }))?.first
        }
    }

    private fun teammates(heroUnit: HeroUnit) = unitsSeq(heroUnit.team).filterNot { it == heroUnit }
}

class DistanceReceiverRealMovement(
    private val travelPower: Int,
    private val obstacles: Map<Position, ChessPiece>,
    private val self: HeroUnit,
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
            self, null -> {
                resultMap[position] = moveStep
                true
            }

            is Obstacle -> {
                resultMap[position] = moveStep.copy(distanceTravel = -1)
                false
            }
            is HeroUnit -> {
                if (obstacle.team == self.team) {
                    resultMap[position] = moveStep.copy(distanceTravel = -1)
                    true
                } else {
                    resultMap[position] = moveStep.copy(distanceTravel = -1)
                    pass
                }
            }
        }
    }

}

private interface ThreatMoves {
    val movablePositions: Sequence<Position>
}

private abstract class ThreatReceiver(private val movementRange: Int) : DistanceReceiver, ThreatMoves {
    protected val resultMap = mutableMapOf<Position, Pair<Boolean, MoveStep>>()
    final override val movablePositions
        get() = resultMap.asSequence().filter { it.value.first }.map { it.key }

    override fun isOverMaxDistance(distance: Int): Boolean {
        return distance > movementRange
    }
}

private class ThreatWithoutPass(
    movementRange: Int,
    private val obstacles: Map<Position, ChessPiece>
) : ThreatReceiver(movementRange) {
    override fun receive(moveStep: MoveStep): Boolean {
        if (resultMap[moveStep.position] != null) {
            return false
        }
        val isNotObstacle = obstacles[moveStep.position] !is Obstacle
        resultMap[moveStep.position] = Pair(isNotObstacle, moveStep)
        return isNotObstacle
    }
}

private class ThreatWithPass(
    private val movementRange: Int,
    private val obstacles: Map<Position, ChessPiece>,
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