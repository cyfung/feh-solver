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

object Sword : WeaponType(Color.RED, false, false)
object Axe : WeaponType(Color.GREEN, false, false)
object Lance : WeaponType(Color.BLUE, false, false)
object Staff : WeaponType(Color.COLORLESS, true, true)

abstract class Dagger(color: Color) : WeaponType(color, true, false)
abstract class Bow(color: Color) : WeaponType(color, true, false)
abstract class Magic(color: Color) : WeaponType(color, true, true)
abstract class Beast(color: Color) : WeaponType(color, false, false)
abstract class Dragon(color: Color) : WeaponType(color, false, true)

object DaggerC : Dagger(Color.COLORLESS)

object BowC : Bow(Color.COLORLESS)
object BowB : Bow(Color.BLUE)

object MagicR : Magic(Color.RED)
object MagicB : Magic(Color.BLUE)
object MagicG : Magic(Color.GREEN)
object MagicC : Magic(Color.COLORLESS)

object DragonR : Dragon(Color.RED)
object DragonB : Dragon(Color.BLUE)
object DragonG : Dragon(Color.GREEN)
object DragonC : Dragon(Color.COLORLESS)


