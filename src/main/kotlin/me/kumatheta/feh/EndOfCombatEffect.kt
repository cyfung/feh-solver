package me.kumatheta.feh

class EndOfCombatEffect {
    var hp: Int = 0
        private set

    fun takeNonLethalDamage(damage: Int) {
        hp -= damage
    }
}
