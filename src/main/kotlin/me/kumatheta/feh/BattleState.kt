package me.kumatheta.feh

import me.kumatheta.feh.skill.*
import me.kumatheta.feh.skill.assist.movement.Pivot
import me.kumatheta.feh.skill.effect.startofturn.MOVE_ORDER_EFFECT
import me.kumatheta.feh.skill.special.Miracle
import me.kumatheta.feh.util.*
import kotlin.math.max
import kotlin.math.min

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
    private val battleMap: FixedBattleMap,
    private val locationMap: MutableMap<Position, ChessPiece>,
    phase: Int,
    playerDied: Int,
    enemyDied: Int,
    winningTeam: Team?,
    engaged: Boolean,
    private val endOnPlayerDeath: Boolean
) {
    val enemyCount
        get() = battleMap.enemyCount
    val playerCount
        get() = battleMap.playerCount

    var playerDied: Int = playerDied
        private set

    var enemyDied: Int = enemyDied
        private set

    var winningTeam: Team? = winningTeam
        private set

    class MovementResult(val gameEnd: Boolean, val teamLostUnit: Team?, val phraseChange: Boolean)

    val maxPosition
        get() = battleMap.maxPosition

    var phase = phase
        private set

    var engaged = engaged
        private set

    val turn: Int
        get() = phase / 2 + 1

    fun copy(): BattleState {
        val newLocationMap = mutableMapOf<Position, ChessPiece>()
        locationMap.mapValuesTo(newLocationMap) { it.value.copy() }
        return BattleState(
            battleMap = battleMap,
            locationMap = newLocationMap,
            phase = phase,
            playerDied = playerDied,
            enemyDied = enemyDied,
            winningTeam = winningTeam,
            engaged = engaged,
            endOnPlayerDeath = endOnPlayerDeath
        )
    }

    private fun copy(chessPieces: Sequence<ChessPiece>): BattleState {
        val newLocationMap = mutableMapOf<Position, ChessPiece>()
        chessPieces.associateByTo(newLocationMap) { it.position }
        return BattleState(
            battleMap = battleMap,
            locationMap = newLocationMap,
            phase = phase,
            playerDied = playerDied,
            enemyDied = enemyDied,
            winningTeam = winningTeam,
            engaged = engaged,
            endOnPlayerDeath = endOnPlayerDeath
        )
    }

    constructor(battleMap: BattleMap, endOnPlayerDeath: Boolean = true) : this(
        battleMap = FixedBattleMap(battleMap),
        locationMap = battleMap.toChessPieceMap().toMutableMap(),
        phase = 0,
        playerDied = 0,
        enemyDied = 0,
        winningTeam = null,
        engaged = false,
        endOnPlayerDeath = endOnPlayerDeath
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
        val heroUnit = try {
            getUnit(unitAction.heroUnitId)
        } catch (e: Exception) {
            println("$unitAction failed, heroUnitId not found, $locationMap")
            throw e
        }
        check(heroUnit.available) {
            "hero not available"
        }
        move(heroUnit, unitAction.moveTarget)
        return when (unitAction) {
            is MoveOnly -> {
                heroUnit.actionEnded()
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
                heroUnit.actionEnded()
                null
            }
            is MoveAndAssist -> {
                val assist = heroUnit.assist ?: throw IllegalStateException()
                val target = try {
                    getUnit(unitAction.assistTargetId)
                } catch (e: Exception) {
                    println("$unitAction failed, assist not found, $locationMap")
                    throw e
                }
                assist.apply(heroUnit, target, this)
                heroUnit.skillSet.assistRelated.forEach {
                    it.apply(this, heroUnit, target, assist, true)
                }
                target.skillSet.assistRelated.forEach {
                    it.apply(this, target, heroUnit, assist, false)
                }
                heroUnit.actionEnded()
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
        val nextTeam = if (++phase % 2 == 0) {
            Team.PLAYER
        } else {
            Team.ENEMY
        }
        unitsSeq(nextTeam.foe).forEach {
            it.endOfTurn()
        }
        if (nextTeam == Team.PLAYER) {
            battleMap.reinforceByTime[turn]?.forEach { it ->
                val newUnit = it.copy()
                if (locationMap.putIfAbsent(newUnit.position, newUnit) != null) {
                    val pathAvailable = distanceFrom(newUnit, newUnit.position).keys
                    val actualLocation = battleMap.terrainMap.asSequence().filter {
                        locationMap[it.key] == null
                    }.filterNot {
                        it.value.type == Terrain.Type.WALL
                    }.minWith(
                        compareBy({
                            when {
                                pathAvailable.contains(it.key) -> 0
                                it.value.moveCost(newUnit.moveType) != null -> 1
                                else -> 2
                            }
                        }, {
                            it.key.distanceTo(newUnit.position)
                        }, {
                            it.key
                        })
                    )?.key ?: throw IllegalStateException("whole map full?")
                    if (locationMap.putIfAbsent(actualLocation, newUnit) != null) {
                        throw IllegalStateException()
                    }
                    newUnit.position = actualLocation
                }
            }
        }
        startOfTurn(nextTeam)
    }

    internal fun swap(unit1: HeroUnit, unit2: HeroUnit) {
        val unit1OldPosition = unit1.position
        val unit2OldPosition = unit2.position
        require(locationMap.put(unit2OldPosition, unit1) == unit2)
        require(locationMap.put(unit1OldPosition, unit2) == unit1)
        unit1.position = unit2OldPosition
        unit2.position = unit1OldPosition
    }

    internal fun rearrange(order: List<Int>) {
        if (order.size != playerCount || order.any { it < 1 || it > playerCount }) {
            throw IllegalArgumentException("invalid order $order")
        }
        val units = (1..playerCount).map {
            getUnit(it)
        }
        val positions = units.map {
            it.position
        }
        order.forEachIndexed { index, id ->
            val heroUnit = units[id - 1]
            val position = positions[index]
            locationMap[position] = heroUnit
            heroUnit.position = position
        }
    }

    internal fun move(heroUnit: HeroUnit, position: Position) {
        val originalPosition = heroUnit.position
        if (originalPosition != position) {
            val piece = locationMap.putIfAbsent(position, heroUnit)
            require(piece == null) {
                "${heroUnit.name} move to $position occupied by ${(piece as? HeroUnit)?.name ?: "Obstacle"}"
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
                it(this, heroUnit)
            }
        }
        units.forEach(HeroUnit::applyCachedEffect)
        return units
    }

    private class PotentialDamage(
        val attackerInCombat: FullInCombatStat,
        val defenderInCombat: FullInCombatStat,
        val attackOrder: List<Boolean>,
        val colorAdvantage: Int,
        val potentialDamage: Int
    )

    private fun preCalculateDamage(
        attacker: HeroUnit,
        defender: HeroUnit
    ): PotentialDamage {
        val skillWrappers = getInCombatStat(attacker, defender)

        val neutralizeFollowUp = skillWrappers.attacker.neutralizeFollowUp || skillWrappers.defender.neutralizeFollowUp

        // check adaptive
        val attackerAdaptive = skillWrappers.attacker.adaptiveDamage &&
                !skillWrappers.defender.denyAdaptiveDamage
        val defenderAdaptive = skillWrappers.defender.adaptiveDamage &&
                !skillWrappers.attacker.denyAdaptiveDamage

        // check staff damage
        val attackerReducedStaffDamage = if (attacker.weaponType == Staff) {
            !skillWrappers.attacker.staffAsNormal || skillWrappers.defender.denyStaffAsNormal
        } else {
            false
        }
        val defenderReducedStaffDamage = if (defender.weaponType == Staff) {
            !skillWrappers.defender.staffAsNormal || skillWrappers.attacker.denyStaffAsNormal
        } else {
            false
        }

        val colorAdvantage = getColorAdvantage(skillWrappers)

        val spdDiff =
            skillWrappers.attacker.inCombatStat.inCombatStat.spd - skillWrappers.defender.inCombatStat.inCombatStat.spd

        val attackerInCombat = FullInCombatStat(
            skills = skillWrappers.attacker,
            adaptiveDamage = attackerAdaptive,
            reducedStaffDamage = attackerReducedStaffDamage
        )
        val defenderInCombat = FullInCombatStat(
            skills = skillWrappers.defender,
            adaptiveDamage = defenderAdaptive,
            reducedStaffDamage = defenderReducedStaffDamage
        )

        val attackOrder = createAttackOrder(attackerInCombat, defenderInCombat, spdDiff, neutralizeFollowUp)

        val potentialDamage = attackOrder.asSequence().filter { it }.count() * calculateBaseDamage(
            attackerInCombat,
            defenderInCombat,
            colorAdvantage = colorAdvantage,
            damagingSpecial = null
        ).first
        return PotentialDamage(attackerInCombat, defenderInCombat, attackOrder, colorAdvantage, potentialDamage)
    }

    private fun getAttackerSkillSeq(
        attacker: HeroUnit,
        defender: HeroUnit,
        attackerTeammates: List<SupportCombatInput>,
        defenderTeammates: List<SupportCombatInput>
    ): Sequence<Skill> {
        val attackerTeamSkills = attackerTeammates.asSequence().flatMap { supportCombatInput ->
            supportCombatInput.self.skillSet.supportInCombatBuff.asSequence().map {
                it(supportCombatInput)
            }
        }.filterNotNull()
        val defenderTeamSkills = defenderTeammates.asSequence().flatMap { supportCombatInput ->
            supportCombatInput.self.skillSet.supportInCombatDebuff.asSequence().map {
                it(supportCombatInput)
            }
        }.filterNotNull()
        return attacker.skillSet.skills.asSequence() + attackerTeamSkills + defenderTeamSkills + defender.skillSet.foeEffect.asSequence().map {
            it(CombatStatus(this, defender, attacker, false))
        }.filterNotNull()
    }

    private fun getDefenderSkillSeq(
        attacker: HeroUnit,
        defender: HeroUnit,
        attackerTeammates: List<SupportCombatInput>,
        defenderTeammates: List<SupportCombatInput>
    ): Sequence<Skill> {
        val defenderTeamSkills = defenderTeammates.asSequence().flatMap { supportCombatInput ->
            supportCombatInput.self.skillSet.supportInCombatBuff.asSequence().map {
                it(supportCombatInput)
            }
        }.filterNotNull()
        val attackerTeamSkills = attackerTeammates.asSequence().flatMap { supportCombatInput ->
            supportCombatInput.self.skillSet.supportInCombatDebuff.asSequence().map {
                it(supportCombatInput)
            }
        }.filterNotNull()
        return defender.skillSet.skills.asSequence() + defenderTeamSkills + attackerTeamSkills + attacker.skillSet.foeEffect.asSequence().map {
            it(CombatStatus(this, attacker, defender, true))
        }.filterNotNull()
    }

    private fun HeroUnit.basicStat(
        skills: InCombatSkillSet
    ): BasicInCombatStat {
        val modifiedBonus = skills.neutralizeBonus.filterNotNull().fold(bonus) { acc, stat ->
            acc * stat
        }
        val modifiedPenalty = skills.neutralizePenalty.filterNotNull().fold(penalty) { acc, stat ->
            acc * stat
        }
        val finalStat = baseStat + modifiedBonus + modifiedPenalty + skills.inCombatStat.fold(Stat.ZERO) { acc, stat ->
            acc + stat
        }
        return BasicInCombatStat(
            heroUnit = this,
            bonus = modifiedBonus,
            penalty = modifiedPenalty,
            inCombatStat = finalStat
        )
    }

    private fun getInCombatStat(
        attacker: HeroUnit,
        defender: HeroUnit
    ): AttackerDefenderPair<InCombatSkillWrapper> {
        val skillsPair = getInCombatSkills(attacker, defender)
        val attackerInCombatStat = attacker.basicStat(skillsPair.attacker)
        val defenderInCombatStat = defender.basicStat(skillsPair.defender)
        val attackerSkillWrapper = InCombatSkillWrapper(
            attackerInCombatStat,
            defenderInCombatStat,
            skillsPair.attacker
        )
        val defenderSkillWrapper = InCombatSkillWrapper(
            defenderInCombatStat,
            attackerInCombatStat,
            skillsPair.defender
        )
        attackerInCombatStat.inCombatStat += attackerSkillWrapper.additionalInCombatStat
        defenderInCombatStat.inCombatStat += defenderSkillWrapper.additionalInCombatStat

        if (battleMap.getTerrain(attacker.position).isDefenseTile) {
            attackerInCombatStat.inCombatStat = attackerInCombatStat.inCombatStat.copy(
                def = attackerInCombatStat.inCombatStat.def * 13 / 10,
                res = attackerInCombatStat.inCombatStat.res * 13 / 10
            )
        }

        if (battleMap.getTerrain(defender.position).isDefenseTile) {
            defenderInCombatStat.inCombatStat = defenderInCombatStat.inCombatStat.copy(
                def = defenderInCombatStat.inCombatStat.def * 13 / 10,
                res = defenderInCombatStat.inCombatStat.res * 13 / 10
            )
        }

        return AttackerDefenderPair(
            attackerSkillWrapper,
            defenderSkillWrapper
        )
    }

    private fun getInCombatSkills(attacker: HeroUnit, defender: HeroUnit): AttackerDefenderPair<InCombatSkillSet> {
        val attackerTeammates = unitsSeq(attacker.team).filterNot { it == attacker }.map {
            SupportCombatInput(this, it, attacker, defender)
        }.toList()
        val defenderTeammates = unitsSeq(defender.team).filterNot { it == defender }.map {
            SupportCombatInput(this, it, attacker, defender)
        }.toList()
        val attackerSkills = InCombatSkillSet(
            battleState = this,
            self = attacker,
            foe = defender,
            initAttack = true,
            skills = getAttackerSkillSeq(attacker, defender, attackerTeammates, defenderTeammates)
        )
        val defenderSkills = InCombatSkillSet(
            battleState = this,
            self = defender,
            foe = attacker,
            initAttack = false,
            skills = getDefenderSkillSeq(attacker, defender, attackerTeammates, defenderTeammates)
        )
        return AttackerDefenderPair(attackerSkills, defenderSkills)
    }

    private fun getColorAdvantage(
        skillWrappers: AttackerDefenderPair<InCombatSkillWrapper>
    ): Int {
        val attackerRaven = skillWrappers.attacker.raven
        val defenderRaven = skillWrappers.defender.raven
        val defender = skillWrappers.defender.heroUnit
        val advantage = attackerRaven && defender.weaponType.color == Color.COLORLESS
        val attacker = skillWrappers.attacker.heroUnit
        val disadvantage = defenderRaven && attacker.weaponType.color == Color.COLORLESS
        val colorAdvantage = when {
            advantage ->
                if (disadvantage) {
                    throw IllegalStateException("a unit with colorless advantage is colorless")
                } else {
                    20
                }
            disadvantage -> -20
            else -> attacker.getColorAdvantage(defender)
        }
        if (colorAdvantage == 0) {
            return 0
        }
        val attackerCancelAffinity = skillWrappers.attacker.cancelAffinity
        val defenderCancelAffinity = skillWrappers.defender.cancelAffinity
        if (attackerCancelAffinity > 0 && defenderCancelAffinity > 0) {
            return colorAdvantage
        }
        if (attackerCancelAffinity != 0) {
            return when (attackerCancelAffinity) {
                1 -> colorAdvantage
                2 -> if (colorAdvantage < 0) {
                    colorAdvantage
                } else {
                    val bonus = skillWrappers.defender.triangleAdept
                    colorAdvantage + bonus
                }
                3 -> {
                    val bonus = skillWrappers.defender.triangleAdept
                    colorAdvantage + bonus
                }
                else -> throw IllegalStateException("unknown value for attackerCancelAffinity: $attackerCancelAffinity")
            }
        }
        if (defenderCancelAffinity != 0) {
            return when (defenderCancelAffinity) {
                1 -> colorAdvantage
                2 -> if (colorAdvantage > 0) {
                    colorAdvantage
                } else {
                    val bonus = skillWrappers.attacker.triangleAdept
                    colorAdvantage - bonus
                }
                3 -> {
                    val bonus = skillWrappers.attacker.triangleAdept
                    colorAdvantage - bonus
                }
                else -> throw IllegalStateException("unknown value for attackerCancelAffinity: $attackerCancelAffinity")
            }
        }
        val bonus = maxOf(skillWrappers.attacker.triangleAdept, skillWrappers.defender.triangleAdept)
        return if (colorAdvantage > 0) {
            colorAdvantage + bonus
        } else {
            colorAdvantage - bonus
        }

    }

    private val PotentialDamage.cooldownAmount: CooldownChange<AttackerDefenderPair<Int>>
        get() {
            val attackerCooldownIncrease = attackerInCombat.skills.cooldownBuff
            val attackerGuard = attackerInCombat.skills.cooldownDebuff

            val defenderCooldownIncrease = defenderInCombat.skills.cooldownBuff
            val defenderGuard = defenderInCombat.skills.cooldownDebuff

            return CooldownChange(
                AttackerDefenderPair(
                    1 + attackerCooldownIncrease.unitAttack - defenderGuard.foeAttack,
                    1 + defenderCooldownIncrease.foeAttack - attackerGuard.unitAttack
                ),
                AttackerDefenderPair(
                    1 + attackerCooldownIncrease.foeAttack - defenderGuard.unitAttack,
                    1 + defenderCooldownIncrease.unitAttack - attackerGuard.foeAttack
                )
            )
        }

    private fun fight(attacker: HeroUnit, defender: HeroUnit): Pair<HeroUnit?, Int> {
        check(attacker.team.foe == defender.team)
        if (attacker.cooldown == 0 && attacker.special is AoeSpecial) {
            val attackerStat = attacker.aoeInCombatStat()
            attacker.special.getTargets(this, attacker, defender).forEach { target ->
                val skillsPair = getInCombatSkills(attacker, target)
                val attackerAdaptive = skillsPair.attacker.adaptiveDamage && !skillsPair.defender.denyAdaptiveDamage
                val targetStat = target.aoeInCombatStat()
                val defRes = getDefRes(attacker, attackerAdaptive, targetStat.inCombatStat)
                val atk = attacker.visibleStat.atk
                val baseDamage = max((atk - defRes) * attacker.special.damageFactor / 100, 0)
                val finalDamage = baseDamage + skillsPair.attacker.damageIncrease.asSequence().map {
                    it(CombatStatus(this, attackerStat, targetStat, true), true)
                }.sum()
                target.takeNonLethalDamage(finalDamage)
            }
            attacker.resetCooldown()
        }

        val potentialDamage = preCalculateDamage(attacker, defender)

        val cooldownAmount = potentialDamage.cooldownAmount

        var attackerAttacked = false
        var defenderAttacked = false

        val attackerAttackColorAdv = potentialDamage.colorAdvantage
        val defenderAttackColorAdv = -potentialDamage.colorAdvantage
        val deadUnit = potentialDamage.attackOrder.asSequence().mapNotNull { attackerTurn ->
            if (attackerTurn) {
                attackerAttacked = true
                if (singleAttack(
                        potentialDamage.attackerInCombat,
                        potentialDamage.defenderInCombat,
                        attackerAttackColorAdv,
                        cooldownAmount.unitAttack
                    )
                ) {
                    defender
                } else {
                    null
                }
            } else {
                defenderAttacked = true
                if (singleAttack(
                        potentialDamage.defenderInCombat,
                        potentialDamage.attackerInCombat,
                        defenderAttackColorAdv,
                        cooldownAmount.foeAttack
                    )
                ) {
                    attacker
                } else {
                    null
                }
            }
        }.firstOrNull()

        attacker.actionEnded()
        if (!attacker.isDead) {
            val movementEffect = attacker.skillSet.postInitiateMovement
            if (movementEffect != null && movementEffect.isValidAction(attacker, defender, this, attacker.position)) {
                movementEffect.applyMovement(attacker, defender, this)
            }
        }

        potentialDamage.attackerInCombat.skills.postCombat(attackerAttacked)
        potentialDamage.defenderInCombat.skills.postCombat(defenderAttacked)

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

    private fun HeroUnit.aoeInCombatStat(): InCombatStat {
        val inCombatStat: Stat = if (battleMap.getTerrain(position).isDefenseTile) {
            visibleStat.copy(
                def = visibleStat.def * 13 / 10,
                res = visibleStat.res * 13 / 10
            )
        } else {
            visibleStat
        }
        return object : InCombatStat {
            override val heroUnit: HeroUnit
                get() = this@aoeInCombatStat
            override val bonus: Stat = this@aoeInCombatStat.bonus
            override val penalty: Stat = this@aoeInCombatStat.penalty
            override val inCombatStat: Stat = inCombatStat
        }
    }

    private fun setGroupEngaged(heroUnit: HeroUnit) {
        engaged = true
        val group = heroUnit.group ?: return
        unitsSeq(heroUnit.team).filter {
            it.group == group
        }.forEach {
            it.setEngaged(it == heroUnit)
        }
    }

    private fun singleAttack(
        damageDealer: FullInCombatStat,
        damageReceiver: FullInCombatStat,
        colorAdvantage: Int,
        cooldownAmount: AttackerDefenderPair<Int>
    ): Boolean {
        val damageDealerCooldown = cooldownAmount.attacker
        val damageReceiverCooldown = cooldownAmount.defender
        val damagingSpecial =
            if (damageDealer.heroUnit.cooldown == 0) {
                damageDealer.heroUnit.special as? DamagingSpecial
            } else {
                null
            }
        val (baseDamage, damagingSpecialUsed) = calculateBaseDamage(
            damageDealer, damageReceiver, colorAdvantage, damagingSpecial
        )

        var defenseSpecialUsed = false

        var damage = baseDamage
        if (damageReceiver.heroUnit.cooldown == 0) {
            val defenseSpecial = damageReceiver.heroUnit.special as? DefenseSpecial
            if (defenseSpecial != null && defenseSpecial != Miracle) {
                val newDamage = defenseSpecial.getReducedDamage(this, damageReceiver, damageDealer, damage)
                if (newDamage != null) {
                    damage = newDamage
                    defenseSpecialUsed = true
                }
            }
        }

        damageReceiver.skills.getPercentageDamageReduce(defenseSpecialUsed).forEach {
            damage -= damage * it / 100
        }

        damage -= damageReceiver.skills.getFlatDamageReduce(defenseSpecialUsed)

        if (damage < 0) {
            damage = 0
        }

        val hpBefore = damageReceiver.heroUnit.takeDamage(damage)


        if (damageReceiver.heroUnit.isDead && hpBefore > 1) {
            if (damageReceiver.heroUnit.special == Miracle && damageReceiver.heroUnit.cooldown == 0) {
                damageReceiver.heroUnit.heal(1)
                defenseSpecialUsed = true
            }
        }

        // attacker reduce cooldown when special is not used
        if (damagingSpecial == null) {
            damageDealer.heroUnit.reduceCooldown(damageDealerCooldown)
        } else {
            damageDealer.heroUnit.resetCooldown()
        }

        damage = hpBefore - damageReceiver.heroUnit.currentHp
        val damageDealt = DamageDealt(
            damagingSpecialUsed,
            defenseSpecialUsed,
            damage,
            baseDamage - damage
        )

        damageDealer.skills.damageDealt(damageDealt)
        damageReceiver.skills.damageReceived(damageDealt)

        val dead = damageReceiver.heroUnit.isDead
        if (dead) {
            locationMap.remove(damageReceiver.heroUnit.position)
            val teamDied = damageReceiver.heroUnit.team
            if (teamDied == Team.PLAYER) {
                playerDied++
                if (endOnPlayerDeath) {
                    winningTeam = Team.ENEMY
                }
            } else {
                enemyDied++
            }
            if (unitsSeq(teamDied).none()) {
                winningTeam = teamDied.foe
            }
        } else if (defenseSpecialUsed) {
            damageReceiver.heroUnit.resetCooldown()
        } else {
            damageReceiver.heroUnit.reduceCooldown(damageReceiverCooldown)
        }
        return dead
    }

    private fun createAttackOrder(
        attackerInCombat: FullInCombatStat,
        defenderInCombat: FullInCombatStat,
        spdDiff: Int,
        neutralizeFollowUp: Boolean
    ): List<Boolean> {
        val attacker = attackerInCombat.heroUnit
        val defender = defenderInCombat.heroUnit
        val attackerSkillSet = attackerInCombat.skills
        val defenderSkillSet = defenderInCombat.skills

        val rangeMatch = when {
            defender.isEmptyHanded -> false
            attacker.weaponType.isRanged == defender.weaponType.isRanged -> true
            else -> defenderSkillSet.counterIgnoreRange
        }

        val canCounter = rangeMatch && defenderSkillSet.canCounter

        val disablePriorityChange = attackerSkillSet.disablePriorityChange ||
                defenderSkillSet.disablePriorityChange

        val desperation: Boolean
        val vantage: Boolean
        if (disablePriorityChange) {
            desperation = false
            vantage = false
        } else {
            desperation = attackerSkillSet.desperation
            vantage = defenderSkillSet.vantage
        }

        val attackerFollowup: Boolean
        val defenderFollowup: Boolean
        if (neutralizeFollowUp) {
            when {
                spdDiff >= 5 -> {
                    attackerFollowup = true
                    defenderFollowup = false
                }
                spdDiff <= -5 -> {
                    attackerFollowup = false
                    defenderFollowup = true
                }
                else -> {
                    attackerFollowup = false
                    defenderFollowup = false
                }
            }
        } else {
            attackerFollowup = when (val guarantee =
                attackerSkillSet.followUp) {
                0 -> spdDiff >= 5
                else -> guarantee > 0
            }
            defenderFollowup = when (val guarantee =
                defenderSkillSet.followUp) {
                0 -> spdDiff <= -5
                else -> guarantee > 0
            }
        }

        val attackOrder = mutableListOf<Boolean>()
        val attackerBrave = attackerSkillSet.brave
        val defenderBrave = defenderSkillSet.brave
        val addAttacker = {
            attackOrder.add(true)
            if (attackerBrave) {
                attackOrder.add(true)
            }
        }
        val addDefender = {
            if (canCounter) {
                attackOrder.add(false)
                if (defenderBrave) {
                    attackOrder.add(false)
                }
            }
        }

        // actual add attack orders
        if (vantage) {
            addDefender()
        }
        addAttacker()

        if (attackerFollowup && desperation) {
            addAttacker()
        }

        // normal counter
        if (!vantage) {
            addDefender()
        }

        // vantage followup counter
        if (defenderFollowup && vantage) {
            addDefender()
        }

        if (attackerFollowup && !desperation) {
            addAttacker()
        }

        // normal followup counter
        if (defenderFollowup && !vantage) {
            addDefender()
        }
        return attackOrder.toList()
    }

    private fun calculateBaseDamage(
        damageDealer: FullInCombatStat,
        damageReceiver: FullInCombatStat,
        colorAdvantage: Int,
        damagingSpecial: DamagingSpecial?
    ): Pair<Int, Boolean> {
        val defenderDefRes = getDefRes(damageDealer.heroUnit, damageDealer.adaptiveDamage, damageReceiver.inCombatStat)
        val effAtk = if (isEffective(damageDealer.heroUnit, damageReceiver.heroUnit)) {
            damageDealer.inCombatStat.atk * 3 / 2
        } else {
            damageDealer.inCombatStat.atk
        }
        val atk = if (colorAdvantage != 0) {
            effAtk + effAtk * colorAdvantage / 100
        } else {
            effAtk
        }

        val bonusDamage = damagingSpecial?.getDamage(this, damageDealer, damageReceiver, defenderDefRes, atk) ?: 0

        var damage = max(atk + bonusDamage - defenderDefRes, 0)
        if (damageDealer.reducedStaffDamage) {
            damage /= 2
        }

        damage += damageDealer.skills.getDamageIncrease(damagingSpecial != null)
        return damage to (damagingSpecial != null)
    }

    private fun getDefRes(damageDealer: HeroUnit, damageDealerAdaptiveDamage: Boolean, damageReceiverStat: Stat): Int {
        return if (damageDealerAdaptiveDamage) {
            min(damageReceiverStat.res, damageReceiverStat.def)
        } else {
            val targetRes = damageDealer.weaponType.targetRes
            if (targetRes) {
                damageReceiverStat.res
            } else {
                damageReceiverStat.def
            }
        }
    }

    private fun isEffective(attacker: HeroUnit, defender: HeroUnit): Boolean {
        if (attacker.weaponType is Bow && defender.moveType == MoveType.FLYING) {
            return true
        }
        return attacker.isEffective(defender)
    }

    fun playerMove(unitAction: UnitAction): MovementResult {
        require(isPlayerPhrase) {
            "not player phase"
        }
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

    val isPlayerPhrase
        get() = phase % 2 == 0

    fun enemyMoves(): List<UnitAction> {
        return enemyMoves {
            it
        }
    }

    fun <T : Any> enemyMoves(f: (UnitAction) -> T): List<T> {
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
            val obstruct = foeTeam.obstruct()
            val possibleMoves = possibleMoves(obstruct, foeThreat, availableUnits.asSequence())

            val attackTargetPositions = attackTargetPositions(possibleMoves)

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
                return@generateSequence f(preCombatAssist)
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
                return@generateSequence f(attack.action)
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
                return@generateSequence f(postCombatAssist)
            }

            // aggressive movement assist
            val allyThreat = lazyAllyThreat.value

            val aggressiveAssist = getAggressiveAssist(assistSortedAllies, possibleMoves, foeThreat, allyThreat)

            if (aggressiveAssist != null) {
                executeMove(aggressiveAssist)
                return@generateSequence f(aggressiveAssist)
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
                    moveTargets(enemy, emptySet(), obstaclesOnly)
                }.map {
                    it.position
                }
            }

            val movementAssist =
                getProtectiveMovementAssist(assistSortedAllies, possibleMoves, lazyFoeMeleeMoves, foeThreat)
            if (movementAssist != null) {
                executeMove(movementAssist)
                return@generateSequence f(movementAssist)
            }

            // movement
            val move = getMoveAction(
                availableUnits,
                distanceToClosestFoe,
                possibleMoves,
                attackTargetPositions,
                distanceFromAlly,
                foeThreat,
                obstacles
            )
            if (move != null) {
                executeMove(move)
                return@generateSequence f(move)
            }

            // no action, cleanup
            availableUnits.asSequence().forEach {
                it.actionEnded()
            }
            null
        }.toList()

        turnEnd()
        return movements
    }

    fun dangerAreas(): Map<HeroUnit, Map<Position, MoveStep>> {
        val possibleMoves = possibleMoves(emptySet(), emptyMap(), unitsSeq(Team.ENEMY))
        return attackTargetPositions(possibleMoves)
    }

    private fun attackTargetPositions(possibleMoves: Map<HeroUnit, Map<Position, MoveStep>>): Map<HeroUnit, Map<Position, MoveStep>> {
        return possibleMoves.mapValues { (heroUnit, moves) ->
            moves.values.asSequence().flatMap { moveStep ->
                attackTargetPositions(heroUnit, moveStep.position, maxPosition).mapNotNull {
                    it to moveStep
                }
            }.distinctBy { it.first }.associate { it }
        }
    }

    private fun possibleMoves(
        obstruct: Set<Position>,
        foeThreat: Map<Position, Int>,
        availableUnits: Sequence<HeroUnit>
    ): Map<HeroUnit, Map<Position, MoveStep>> {
        return availableUnits.associateWith { heroUnit ->
            moveTargets(heroUnit, obstruct).sortedWith(attackPositionOrder(heroUnit, foeThreat))
                .associateBy { it.position }
        }
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
        foeThreat: Map<Position, Int>,
        obstacles: Map<Position, ChessPiece>
    ): UnitAction? {
        return availableUnits.asSequence().filter { it.engaged }.sortedWith(unitMoveOrder(distanceToClosestFoe))
            .mapNotNull { heroUnit ->
                val moves = possibleMoves[heroUnit] ?: throw IllegalStateException()
                val distanceMap = distanceFromAlly[heroUnit] ?: throw IllegalStateException()
                val targetPositions = attackTargetPositions[heroUnit] ?: throw IllegalStateException()
                getMoveAction(heroUnit, distanceMap, moves, targetPositions, foeThreat, obstacles)
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
        foeThreat: Map<Position, Int>,
        obstacles: Map<Position, ChessPiece>
    ): UnitAction? {
        val chaseTarget =
            getChaseTarget(heroUnit, distanceMap, obstacles) ?: getClosestAlly(heroUnit, distanceMap) ?: return null

        val distanceToTarget = distanceFrom(heroUnit, chaseTarget.position)
        val okToStay = if (chaseTarget.team == heroUnit.team) {
            unitsSeq(heroUnit.team.foe).mapNotNull { it.position.distanceTo(heroUnit.position) }.all { it > 2 }
        } else {
            true
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
            battleMap.distanceMap[DistanceIndex(it.moveType, it.position, it.weaponType.isRanged)]
                ?: throw IllegalStateException("illegal position of unit")
        }
    }

    private fun distanceFrom(heroUnit: HeroUnit, startingPosition: Position): Map<Position, Int> {
        return battleMap.distanceMap[DistanceIndex(heroUnit.moveType, startingPosition, heroUnit.weaponType.isRanged)]
            ?: throw IllegalStateException("illegal position of unit")
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
        if (assist == null || withIsolation) return emptySequence()
        val teammates = teammates(this).toList()
        return moves.values.asSequence().flatMap { moveStep ->
            teammates.asSequence().filterNot { it.withIsolation }.filterValidAssistTarget(this, moveStep.position).map {
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
            val testUnit = testBattle.getUnit(heroUnitId)
            val testFoe = testBattle.getUnit(foeId)
            val moveAndAttack = MoveAndAttack(heroUnitId, movePosition.position, foeId)
            val (deadUnit, potentialDamage) = testBattle.moveAndFight(moveAndAttack)
            val winLoss = when (deadUnit?.team) {
                heroUnit.team -> WinLoss.LOSS
                null -> WinLoss.DRAW
                else -> WinLoss.WIN
            }
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
            val pass = heroUnit.skillSet.pass.any { it(this, heroUnit) }
            val travelPower = heroUnit.travelPower
            if (pass) {
                val threatReceiver = ThreatWithPass(travelPower, obstacles, heroUnit.team)
                calculateDistance(heroUnit, threatReceiver)
                threatReceiver.movablePositions.flatMap {
                    attackTargetPositions(heroUnit, it, maxPosition)
                }.distinct()
            } else {
                val threatIndex = ThreatIndex(
                    heroUnit.moveType,
                    heroUnit.position,
                    heroUnit.weaponType.isRanged,
                    obstacles.values.asSequence().filterIsInstance<Obstacle>().map { it.position }.toSet()
                )
                val list =
                    battleMap.threatMap[threatIndex] ?: throw IllegalStateException("failed threatIndex $threatIndex")
                list.asSequence().filter { it.distanceTravel <= travelPower }.map { it.position }
            }
        }
    }

    fun getChessPiece(position: Position) = locationMap[position]

    fun getUnit(heroUnitId: Int): HeroUnit =
        locationMap.values.asSequence().filterIsInstance<HeroUnit>().first { it.id == heroUnitId }

    private fun moveTargets(
        heroUnit: HeroUnit,
        obstruct: Set<Position>,
        obstacles: Map<Position, ChessPiece> = locationMap
    ): Sequence<MoveStep> {
        val pass = heroUnit.skillSet.pass.any { it(this, heroUnit) }
        val travelPower = heroUnit.travelPower
        val distanceReceiver = DistanceReceiverRealMovement(travelPower, obstacles, heroUnit, pass, obstruct)
        calculateDistance(heroUnit, distanceReceiver)
        val teleportSkills = heroUnit.skillSet.teleport.asSequence()
        val teleportLocations = if (heroUnit.withMoveOrder) {
            teleportSkills + MOVE_ORDER_EFFECT
        } else {
            teleportSkills
        }.flatMap {
            it(this, heroUnit)
        } + heroUnit.allies(this).filter { ally ->
            ally.skillSet.guidance.any {
                it(this, ally, heroUnit)
            }
        }.flatMap {
            it.position.surroundings(maxPosition)
        }
        teleportLocations.distinct().filter {
            locationMap[it] == null && battleMap.getTerrain(it).moveCost(heroUnit.moveType) != null
        }.map {
            MoveStep(it, battleMap.getTerrain(it), true, 0)
        }.forEach {
            distanceReceiver.receive(it)
        }


        return distanceReceiver.result
    }

    private fun calculateDistance(
        heroUnit: HeroUnit,
        distanceReceiver: DistanceReceiver
    ) {
        val startingPositions = 0 to sequenceOf(
            MoveStep(heroUnit.position, battleMap.getTerrain(heroUnit.position), false, 0)
        )
        battleMap.calculateDistance(
            heroUnit.moveType,
            startingPositions,
            distanceReceiver
        )
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


    fun getAllPlayerMovements(): Sequence<UnitAction> {
        val obstruct = Team.ENEMY.obstruct()
        return unitsSeq(Team.PLAYER).filter { it.available }.flatMap { heroUnit ->
            val teammates = teammates(heroUnit).toList()
            moveTargets(heroUnit, obstruct).map { it.position }.flatMap { move ->
                attackMoves(heroUnit, move) + assistMoves(teammates, heroUnit, move) + MoveOnly(
                    heroUnitId = heroUnit.id,
                    moveTarget = move
                )
            }
        }
    }

    private fun Team.obstruct(): Set<Position> {
        return unitsSeq(this).filter { foe ->
            foe.skillSet.obstruct.any { it(this@BattleState, foe) }
        }.flatMap {
            it.position.surroundings(maxPosition)
        }.toSet()
    }

    private fun assistMoves(teammates: List<HeroUnit>, heroUnit: HeroUnit, move: Position): Sequence<MoveAndAssist> {
        if (heroUnit.assist == null || heroUnit.withIsolation) return emptySequence()
        return teammates.asSequence().filterValidAssistTarget(heroUnit, move).map { assistTarget ->
            MoveAndAssist(
                heroUnitId = heroUnit.id,
                moveTarget = move,
                assistTargetId = assistTarget.id
            )
        }
    }

    private fun attackMoves(heroUnit: HeroUnit, move: Position): Sequence<UnitAction> {
        return attackTargetPositions(heroUnit, move, maxPosition).mapNotNull { attackTargetPosition ->
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
        }
    }

    private fun getChaseTarget(
        heroUnit: HeroUnit,
        distanceTo: Map<Position, Int>,
        obstacles: Map<Position, ChessPiece>
    ): HeroUnit? {
        return if (heroUnit.isEmptyHanded) {
            null
        } else {
            val distanceToSeq = if (heroUnit.skillSet.pass.any { it(this, heroUnit) }) {
                distanceTo.asSequence().filter {
                    val chessPiece = obstacles[it.key]
                    chessPiece == null || (chessPiece is HeroUnit && chessPiece.team == heroUnit.team)
                }
            } else {
                distanceTo.asSequence()
            }
            val chaseTargets = distanceToSeq.flatMap { (position, distance) ->
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
                    ).potentialDamage - (distance / heroUnit.travelPower) * 5
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
    private val pass: Boolean,
    private val obstruct: Set<Position>
) : DistanceReceiver {
    private val resultMap = mutableMapOf<Position, MoveStep>()
    override fun isOverMaxDistance(distance: Int): Boolean {
        return distance > travelPower
    }

    val result
        get() = resultMap.values.asSequence().filter {
            it.distanceTravel >= 0
        }

    private val withGravity = self.withNegativeStatus(NegativeStatus.GRAVITY)

    private fun MoveStep.obstruct(): Boolean {
        return if (pass || teleportRequired || distanceTravel == 0) {
            true
        } else {
            !obstruct.contains(position)
        }
    }

    override fun receive(moveStep: MoveStep): Boolean {
        val position = moveStep.position
        if (resultMap[position] != null) {
            return false
        }
        if (withGravity && !moveStep.teleportRequired && position.distanceTo(self.position) > 1) {
            return false
        }
        return when (val obstacle = obstacles[position]) {
            self, null -> {
                resultMap[position] = moveStep
                moveStep.obstruct()
            }

            is Obstacle -> {
                resultMap[position] = moveStep.copy(distanceTravel = -1)
                false
            }
            is HeroUnit -> {
                if (obstacle.team == self.team) {
                    resultMap[position] = moveStep.copy(distanceTravel = -1)
                    moveStep.obstruct()
                } else {
                    resultMap[position] = moveStep.copy(distanceTravel = -1)
                    pass
                }
            }
        }
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