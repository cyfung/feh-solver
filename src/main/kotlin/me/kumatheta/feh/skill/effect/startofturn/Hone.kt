package me.kumatheta.feh.skill.effect.startofturn

import me.kumatheta.feh.BattleState
import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.MoveType
import me.kumatheta.feh.Stat
import me.kumatheta.feh.WeaponType
import me.kumatheta.feh.skill.adjacentAllies
import me.kumatheta.feh.skill.effect.StartOfTurnEffect

fun hone(buff: Stat) = object : StartOfTurnEffect {
    override fun onStartOfTurn(battleState: BattleState, self: HeroUnit) {
        self.adjacentAllies(battleState).forEach {
            it.cachedEffect.applyBuff(buff)
        }
    }
}

fun hone(buff: Stat, moveType: MoveType): StartOfTurnEffect = object : StartOfTurnEffect {
    override fun onStartOfTurn(battleState: BattleState, self: HeroUnit) {
        self.adjacentAllies(battleState).filter { it.moveType == moveType }.forEach {
            it.cachedEffect.applyBuff(buff)
        }
    }
}

inline fun <reified W : WeaponType> honeWeaponType(buff: Stat): StartOfTurnEffect = object : StartOfTurnEffect {
    override fun onStartOfTurn(battleState: BattleState, self: HeroUnit) {
        self.adjacentAllies(battleState).filter { it.weaponType is W }.forEach {
            it.cachedEffect.applyBuff(buff)
        }
    }
}

fun hone(
    atk: Int = 0,
    spd: Int = 0,
    def: Int = 0,
    res: Int = 0,
    moveType: MoveType? = null
) = if (moveType == null) {
    hone(Stat(atk = atk, spd = spd, def = def, res = res))
} else {
    hone(Stat(atk = atk, spd = spd, def = def, res = res), moveType = moveType)
}

inline fun <reified W : WeaponType> honeWeaponType(
    atk: Int = 0,
    spd: Int = 0,
    def: Int = 0,
    res: Int = 0
) : StartOfTurnEffect = honeWeaponType<W>(Stat(atk = atk, spd = spd, def = def, res = res))