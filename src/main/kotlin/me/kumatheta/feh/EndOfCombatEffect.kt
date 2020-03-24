package me.kumatheta.feh

class CachedEffect {
    var updated = false
        private set
    var cooldown: Int = 0
        set(value) {
            field = value
            updated = true
        }
    var hp: Int = 0
        private set
    var debuff: Stat = Stat.ZERO
        private set
    var buff: Stat = Stat.ZERO
        private set

    fun takeNonLethalDamage(damage: Int) {
        hp -= damage
        updated = true
    }

    fun heal(healAmount: Int) {
        hp += healAmount
        updated = true
    }

    fun applyDebuff(stat: Stat) {
        debuff = min(debuff, stat)
        updated = true
    }

    fun applyBuff(stat: Stat) {
        buff = max(buff, stat)
        updated = true
    }

}
