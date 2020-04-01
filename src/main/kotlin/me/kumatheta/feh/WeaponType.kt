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

interface FreeColorWeapon {
    fun toColor(color: Color): WeaponType
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

val allDragonType = listOf(DragonR, DragonG, DragonB, DragonC)

object DaggerR : Bow(Color.RED)
object DaggerB : Bow(Color.BLUE)
object DaggerG : Bow(Color.GREEN)
object DaggerC : Dagger(Color.COLORLESS), FreeColorWeapon {
    override fun toColor(color: Color): WeaponType {
        return when (color) {
            Color.RED -> DaggerR
            Color.GREEN -> DaggerG
            Color.BLUE -> DaggerB
            Color.COLORLESS -> this
        }
    }
}

object BowR : Bow(Color.RED)
object BowB : Bow(Color.BLUE)
object BowG : Bow(Color.GREEN)
object BowC : Bow(Color.COLORLESS), FreeColorWeapon {
    override fun toColor(color: Color): WeaponType {
        return when (color) {
            Color.RED -> BowR
            Color.GREEN -> BowG
            Color.BLUE -> BowB
            Color.COLORLESS -> this
        }
    }
}

object MagicR : Magic(Color.RED)
object MagicB : Magic(Color.BLUE)
object MagicG : Magic(Color.GREEN)
object MagicC : Magic(Color.COLORLESS)

object DragonR : Dragon(Color.RED)
object DragonB : Dragon(Color.BLUE)
object DragonG : Dragon(Color.GREEN)
object DragonC : Dragon(Color.COLORLESS), FreeColorWeapon {
    override fun toColor(color: Color): WeaponType {
        return when (color) {
            Color.RED -> DragonR
            Color.GREEN -> DragonG
            Color.BLUE -> DragonB
            Color.COLORLESS -> this
        }
    }
}

object BeastR : Beast(Color.RED)
object BeastB : Beast(Color.BLUE)
object BeastG : Beast(Color.GREEN)
object BeastC : Beast(Color.COLORLESS), FreeColorWeapon {
    override fun toColor(color: Color): WeaponType {
        return when (color) {
            Color.RED -> BeastR
            Color.GREEN -> BeastG
            Color.BLUE -> BeastB
            Color.COLORLESS -> this
        }
    }
}


