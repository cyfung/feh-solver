package me.kumatheta.feh.util

import me.kumatheta.feh.*

val attackTargetOrder = compareByDescending<CombatResult>(
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

val attackerOrder = compareByDescending<CombatResult>(
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
        when (it.heroUnit.specialDebuffer) {
            SpecialDebuff.ALWAYS_AVAILABLE -> 1
            SpecialDebuff.ONLY_WHEN_ALIVE -> if (it.winLoss != WinLoss.LOSS) 1 else 0
            null -> 0
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

fun attackPositionOrder(heroUnit: HeroUnit, enemyThreat: Map<Position, Int>) = compareBy<MoveStep>(
    {
        if (it.terrain.isDefenseTile) 0 else 1
    },
    {
        enemyThreat[it.position] ?: 0
    },
    {
        if (it.teleportRequired) 0 else 1
    },
    {
        it.terrain.priority(heroUnit.moveType)
    },
    {
        it.distanceTravel
    },
    {
        it.position
    }
)

fun unitMoveOrder(distanceToClosestFoe: Map<HeroUnit, Int>): Comparator<HeroUnit> {
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

fun moveTargetOrder(
    heroUnit: HeroUnit,
    foeThreat: Map<Position, Int>,
    chaseTarget: HeroUnit,
    distanceToTarget: Map<Position, Int>
): Comparator<MoveStep> {
    return compareBy({
        distanceToTarget[it.position] ?: throw IllegalStateException()
    }, {
        if (it.terrain.isDefenseTile) {
            0
        } else {
            1
        }
    }, {
        foeThreat[it.position] ?: 0
    }, {
        if (it.teleportRequired) {
            0
        } else {
            1
        }
    }, {
        chaseTarget.position.diagonal(it.position)
    }, {
        it.terrain.priority(heroUnit.moveType)
    }, {
        it.distanceTravel
    }, {
        it.position
    })
}

val bodyBlockTargetOrder = compareByDescending<HeroUnit>({
    it.visibleStat.totalExceptHp
}, {
    if (it.weaponType.isRanged) {
        0
    } else {
        1
    }
}, {
    it.id
})
