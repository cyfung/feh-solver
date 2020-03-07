package me.kumatheta.feh.skill

import me.kumatheta.feh.*

val EmptySkill = BasicSkill()

data class BasicSkill(
    override val postInitiateMovement: MovementEffect? = null,
    override val extraStat: Stat? = null,
    override val phantomStat: Stat? = null,
    override val coolDownCountAdj: Int = 0,
    override val debuffer: Boolean = false,
    override val specialDebuff: SpecialDebuff? = null,
    override val neutralizeEffectiveWeaponType: Set<WeaponType>? = null,
    override val neutralizeEffectiveMoveType: Set<MoveType>? = null,
    override val effectiveAgainstWeaponType: Set<WeaponType>? = null,
    override val effectiveAgainstMoveType: Set<MoveType>? = null,

    // outside of combat
    override val startOfTurn: MapSkillMethod<Unit>? = null,
    override val pass: MapSkillMethod<Boolean>? = null,
    override val obstruct: MapSkillMethod<Boolean>? = null,
    override val teleport: MapSkillMethod<Sequence<Position>>? = null,
    override val guidance: ((battleState: BattleState, self: HeroUnit, target: HeroUnit) -> Boolean)? = null,
    override val supportInCombatBuff: SupportCombatEffect? = null,
    override val supportInCombatDebuff: SupportCombatEffect? = null,
    override val onHealOthers: ((battleState: BattleState, self: HeroUnit, target: HeroUnit, healAmount: Int) -> Unit)? = null,

    override val assistRelated: AssistRelated? = null,

    // very beginning of combat
    override val adaptiveDamage: CombatStartSkill<Boolean>? = null,
    override val denyAdaptiveDamage: CombatStartSkill<Boolean>? = null,
    override val foeEffect: CombatStartSkill<Skill?>? = null,
    override val neutralizeFollowUp: CombatStartSkill<Boolean>? = null,
    override val neutralizeBonus: CombatStartSkill<Stat?>? = null,
    override val neutralizePenalty: CombatStartSkill<Stat?>? = null,
    override val inCombatStat: CombatStartSkill<Stat>? = null,
    override val additionalInCombatStat: InCombatSkill<Stat>? = null,
    override val counter: CombatStartSkill<Int>? = null,
    override val followUp: CombatStartSkill<Int>? = null,
    override val desperation: CombatStartSkill<Boolean>? = null,
    override val vantage: CombatStartSkill<Boolean>? = null,

    // actual in combat
    override val counterIgnoreRange: InCombatSkill<Boolean>? = null,
    override val brave: InCombatSkill<Boolean>? = null,
    override val disablePriorityChange: InCombatSkill<Boolean>? = null,
    override val cooldownBuff: InCombatSkill<CooldownChange<Int>?>? = null,
    override val cooldownDebuff: InCombatSkill<CooldownChange<Int>?>? = null,
    override val triangleAdept: InCombatSkill<Int>? = null,
    override val cancelAffinity: InCombatSkill<Int>? = null,
    override val raven: InCombatSkill<Boolean>? = null,
    override val staffAsNormal: InCombatSkill<Boolean>? = null,
    override val denyStaffAsNormal: InCombatSkill<Boolean>? = null,

    // per attack skill
    override val percentageDamageReduce: ((CombatStatus<InCombatStat>, specialTriggered: Boolean) -> Int)? = null,
    override val flatDamageReduce: ((CombatStatus<InCombatStat>, specialTriggered: Boolean) -> Int)? = null,
    override val damageIncrease: ((CombatStatus<InCombatStat>, specialTriggered: Boolean) -> Int)? = null,

    // listener
    override val damageDealtListener: PerAttackListener<DamageDealt>? = null,
    override val damageReceivedListener: PerAttackListener<DamageDealt>? = null,

    // combat end
    override val combatEnd: CombatEndSkill? = null
) : Skill
