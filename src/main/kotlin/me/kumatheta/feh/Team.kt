package me.kumatheta.feh

enum class Team {
    PLAYER,
    ENEMY;
}

val Team.foe: Team
    get() {
        return when (this) {
            Team.PLAYER -> Team.ENEMY
            Team.ENEMY -> Team.PLAYER
        }
    }