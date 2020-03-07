package me.kumatheta.feh.skill.special

import me.kumatheta.feh.skill.AoeSpecial
import me.kumatheta.feh.BattleState
import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.skill.adjacentAllies
import me.kumatheta.feh.skill.allies
import kotlin.math.absoluteValue

object BlazingThunder : AoeSpecial(4) {
    override val damageFactor: Int
        get() = 150

    override fun getTargets(battleState: BattleState, self: HeroUnit, mainTarget: HeroUnit): Sequence<HeroUnit> {
        return mainTarget.allies(battleState).filter {
            it.position.x  == mainTarget.position.x &&
                    (it.position.y - mainTarget.position.y).absoluteValue <= 2
        } + mainTarget
    }
}

object GrowingThunder : AoeSpecial(4) {
    override val damageFactor: Int
        get() = 100

    override fun getTargets(battleState: BattleState, self: HeroUnit, mainTarget: HeroUnit): Sequence<HeroUnit> {
        return mainTarget.allies(battleState).filter {
            (it.position.x  == mainTarget.position.x &&
                    (it.position.y - mainTarget.position.y).absoluteValue <= 3) ||
                    (it.position.y == mainTarget.position.y &&
                            (it.position.x - mainTarget.position.x).absoluteValue ==1)

        } + mainTarget
    }
}

object BlazingLight : AoeSpecial(4) {
    override val damageFactor: Int
        get() = 150

    override fun getTargets(battleState: BattleState, self: HeroUnit, mainTarget: HeroUnit): Sequence<HeroUnit> {
        return mainTarget.allies(battleState).filter {
            (it.position.x - mainTarget.position.x).absoluteValue == 1 &&
                (it.position.y - mainTarget.position.y).absoluteValue == 1
        } + mainTarget
    }
}

object BlazingWind : AoeSpecial(4) {
    override val damageFactor: Int
        get() = 150

    override fun getTargets(battleState: BattleState, self: HeroUnit, mainTarget: HeroUnit): Sequence<HeroUnit> {
        return mainTarget.adjacentAllies(battleState) + mainTarget
    }
}
