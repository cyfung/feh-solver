package me.kumatheta.feh.skill.assist

import me.kumatheta.feh.HeroUnit

object Reconcile : Heal(7) {
    override fun healAmount(self: HeroUnit, target: HeroUnit): Int {
        return healAmount(7, self, target)
    }
}