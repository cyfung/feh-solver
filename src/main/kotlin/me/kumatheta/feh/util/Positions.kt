package me.kumatheta.feh.util

import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.Position

fun attackTargetPositions(
    heroUnit: HeroUnit,
    standingPosition: Position,
    max: Position
): Sequence<Position> {
    val isRanged = heroUnit.weaponType.isRanged
    return if (isRanged) {
        standingPosition.surroundings(max).flatMap { position -> position.surroundings(max) }
    } else {
        standingPosition.surroundings(max)
    }
}

fun Position.surroundings(max: Position): Sequence<Position> {
    return sequence {
        if (x > 0) {
            yield(Position(x - 1, y))
        }
        if (y > 0) {
            yield(Position(x, y - 1))
        }
        if (x < max.x) {
            yield(Position(x + 1, y))
        }
        if (y < max.y) {
            yield(Position(x, y + 1))
        }
    }
}