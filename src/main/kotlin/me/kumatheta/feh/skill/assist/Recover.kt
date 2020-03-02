package me.kumatheta.feh.skill.assist

import me.kumatheta.feh.HeroUnit

object Recover : Heal(10) {
    override fun healAmount(self: HeroUnit, target: HeroUnit): Int {
        return healAmount(15, self, target)
    }
}

object RecoverPlus : Heal(10) {
    override fun healAmount(self: HeroUnit, target: HeroUnit): Int {
        return maxOf(self.visibleStat.atk / 2 + 10, healAmount(15, self, target))
    }
}