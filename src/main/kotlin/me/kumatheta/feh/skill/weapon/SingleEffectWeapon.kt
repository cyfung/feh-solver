package me.kumatheta.feh.skill.weapon

import me.kumatheta.feh.Stat
import me.kumatheta.feh.WeaponType
import me.kumatheta.feh.skill.*

fun WeaponType.withInCombatStat(
    might: Int,
    inCombatStat: CombatStartSkill<Stat>,
    hp: Int = 0,
    spd: Int = 0,
    def: Int = 0,
    res: Int = 0
): BasicWeapon =
    BasicWeapon(
        this, BasicSkill(
            extraStat = weaponStat(might = might, hp = hp, spd = spd, def = def, res = res),
            inCombatStat = inCombatStat
        )
    )

fun WeaponType.withCombatEndSkill(
    might: Int,
    combatEnd: CombatEndSkill,
    hp: Int = 0,
    spd: Int = 0,
    def: Int = 0,
    res: Int = 0
): BasicWeapon =
    BasicWeapon(
        this, BasicSkill(
            extraStat = weaponStat(might = might, hp = hp, spd = spd, def = def, res = res),
            combatEnd = combatEnd
        )
    )

fun WeaponType.withTriangleAdept(
    might: Int,
    triangleAdept: InCombatSkill<Int>?,
    hp: Int = 0,
    spd: Int = 0,
    def: Int = 0,
    res: Int = 0
): BasicWeapon =
    BasicWeapon(
        this, BasicSkill(
            extraStat = weaponStat(might = might, hp = hp, spd = spd, def = def, res = res),
            triangleAdept = triangleAdept
        )
    )

fun WeaponType.withRaven(
    might: Int,
    raven: InCombatSkill<Boolean>?,
    hp: Int = 0,
    spd: Int = 0,
    def: Int = 0,
    res: Int = 0
): BasicWeapon =
    BasicWeapon(
        this, BasicSkill(
            extraStat = weaponStat(might = might, hp = hp, spd = spd, def = def, res = res),
            raven = raven
        )
    )