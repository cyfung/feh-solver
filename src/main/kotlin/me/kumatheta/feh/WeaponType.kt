package me.kumatheta.feh

enum class Color {
    RED,
    GREEN,
    BLUE,
    COLORLESS,
}

sealed class WeaponType(val color: Color, val isRanged: Boolean, val targetRes: Boolean) {
    val range = if (isRanged) 2 else 1
}

class Beast(color: Color) : WeaponType(color, false, false)

class Dragon(color: Color) : WeaponType(color, false, true)

object Sword : WeaponType(Color.RED, false, false)
object Axe : WeaponType(Color.GREEN, false, false)
object Lance : WeaponType(Color.BLUE, false, false)
object Staff : WeaponType(Color.COLORLESS, true, true)
class Dagger(color: Color) : WeaponType(color, true, false)
class Bow(color: Color) : WeaponType(color, true, false)

class Magic(color: Color) : WeaponType(color, true, true)