package me.kumatheta.feh

class CachedEffect {
    var cooldown: Int = 0
    var hp: Int = 0
        private set

    fun takeNonLethalDamage(damage: Int) {
        hp -= damage
    }

    fun heal(healAmount: Int) {
        hp += healAmount
    }

}
