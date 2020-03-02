package me.kumatheta.feh.skill

class SkillSet(skills: Sequence<Skill>) {
    constructor(skills: List<Skill>) : this(skills.asSequence())

    val skills = skills.toList()

    val postInitiateMovement: MovementEffect?

    init {
        val movements = this.skills.mapNotNull(Skill::postInitiateMovement)
        if (movements.size > 1) {
            throw IllegalStateException("more than one possible movement post combat")
        }
        postInitiateMovement = movements.singleOrNull()
    }

    val startOfTurn = this.skills.mapNotNull(Skill::startOfTurn)
    val pass = this.skills.mapNotNull(Skill::pass)
    val obstruct = this.skills.mapNotNull(Skill::obstruct)
    val teleport = this.skills.mapNotNull(Skill::teleport)
    val guidance = this.skills.mapNotNull(Skill::guidance)

    val foeEffect = this.skills.mapNotNull(Skill::foeEffect)

    val supportInCombatBuff = this.skills.mapNotNull(Skill::supportInCombatBuff)
    val supportInCombatDebuff = this.skills.mapNotNull(Skill::supportInCombatDebuff)
    val onHealOthers = this.skills.mapNotNull(Skill::onHealOthers)

    val assistRelated = this.skills.mapNotNull(Skill::assistRelated)

    fun <T> groupAsSet(f: (Skill) -> Set<T>?): Set<T> {
        return skills.asSequence().mapNotNull(f).flatMap { it.asSequence() }.toSet()
    }
}