package me.kumatheta.feh

class CombatResult(
    val heroUnit: HeroUnit,
    val foe: HeroUnit,
    val winLoss: WinLoss,
    val debuffSuccess: Boolean,
    val potentialDamage: Int,
    val damageDealt: Int,
    val damageReceived: Int,
    val cooldownChange: Int,
    val cooldownChangeFoe: Int,
    val action: MoveAndAttack
) {
    val damageRatio = -(damageDealt * 3 - damageReceived)
}

enum class WinLoss {
    LOSS,
    DRAW,
    WIN
}