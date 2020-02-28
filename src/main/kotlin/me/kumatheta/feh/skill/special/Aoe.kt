package me.kumatheta.feh.skill.special

import me.kumatheta.feh.AoeSpecial
import me.kumatheta.feh.BattleState
import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.skill.allies
import kotlin.math.absoluteValue

object BlazingThunder : AoeSpecial(4) {
    override val damageFactor: Int
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

    override fun getTargets(battleState: BattleState, self: HeroUnit, mainTarget: HeroUnit): Sequence<HeroUnit> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
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