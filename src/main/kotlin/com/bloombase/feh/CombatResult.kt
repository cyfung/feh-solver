package com.bloombase.feh

class CombatResult(
    val heroUnit: HeroUnit,
    val foe: HeroUnit,
    val winLoss: WinLoss,
    val debuffSuccess: Int,
    val potentialDamage: Int,
    val damageDealt: Int,
    val damageReceived: Int,
    val cooldownChange: Int,
    val cooldownChangeFoe: Int
) {
    val damageRatio = -(damageDealt * 3 - damageReceived)
}

enum class WinLoss {
    WIN,
    DRAW,
    LOSS
}