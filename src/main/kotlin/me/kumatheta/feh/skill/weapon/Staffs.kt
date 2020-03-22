package me.kumatheta.feh.skill.weapon

import me.kumatheta.feh.NegativeStatus
import me.kumatheta.feh.skill.effect.SpecialDebuff
import me.kumatheta.feh.Staff
import me.kumatheta.feh.Stat
import me.kumatheta.feh.skill.*
import me.kumatheta.feh.skill.effect.aoeDebuff
import me.kumatheta.feh.skill.effect.aoeNegativeStatus

val DISABLE_COUNTER_EFFECT: CombatStartSkill<Skill?> =
    combatStartSkill(BasicSkill(counter = combatStartSkill(-1)))

private fun staff(
    might: Int,
    combatEnd: CombatEndSkill,
    disableCounter: Boolean = false,
    staffAsNormal: Boolean = false,
    specialDebuff: SpecialDebuff? = null,
    hp: Int = 0,
    spd: Int = 0,
    def: Int = 0,
    res: Int = 0
): BasicWeapon =
    BasicWeapon(
        Staff, BasicSkill(
            extraStat = weaponStat(might = might, hp = hp, spd = spd, def = def, res = res),
            combatEnd = combatEnd,
            foeEffect = if (disableCounter) {
                DISABLE_COUNTER_EFFECT
            } else {
                null
            }, staffAsNormal = if (staffAsNormal) {
                inCombatSkillTrue
            } else {
                null
            },
            specialDebuff = specialDebuff
        )
    )

val slowPlus = staff(12, aoeDebuff(2, true, Stat(spd = -7)))
val gravityPlus = staff(10, combatEnd = aoeNegativeStatus(NegativeStatus.GRAVITY, 1, true))

fun trilemmaPlus(disableCounter: Boolean = false, staffAsNormal: Boolean = false) = staff(
    12,
    combatEnd = aoeNegativeStatus(NegativeStatus.TRIANGLE, 2, true),
    specialDebuff = SpecialDebuff.ALWAYS_AVAILABLE,
    disableCounter = disableCounter,
    staffAsNormal = staffAsNormal
)

val Pain = staff(3, combatEnd = { combatStatus, attacked ->
    if (attacked && !combatStatus.foe.heroUnit.isDead) {
        combatStatus.foe.heroUnit.cachedEffect.takeNonLethalDamage(10)
    }
})