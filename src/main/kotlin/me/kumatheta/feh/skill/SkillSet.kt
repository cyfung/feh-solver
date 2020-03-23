package me.kumatheta.feh.skill

import me.kumatheta.feh.skill.effect.AssistEffect
import me.kumatheta.feh.skill.effect.EffectOnFoe
import me.kumatheta.feh.skill.effect.GuidanceEffect
import me.kumatheta.feh.skill.effect.HealEffect
import me.kumatheta.feh.skill.effect.InCombatSupport
import me.kumatheta.feh.skill.effect.ObstructEffect
import me.kumatheta.feh.skill.effect.PassEffect
import me.kumatheta.feh.skill.effect.PostInitiateMovement
import me.kumatheta.feh.skill.effect.SkillEffect
import me.kumatheta.feh.skill.effect.StartOfTurnEffect
import me.kumatheta.feh.skill.effect.TeleportEffect

class SkillSet(skills: Sequence<SkillEffect>) {
    private val skillEffects = skills.toList()

    val postInitiateMovement: MovementEffect?

    init {
        val movements = this.skillEffects.filterIsInstance<PostInitiateMovement>()
        if (movements.size > 1) {
            throw IllegalStateException("more than one possible movement post combat")
        }
        postInitiateMovement = movements.singleOrNull()?.movementEffect
    }

    val startOfTurn = this.skillEffects.filterIsInstance<StartOfTurnEffect>()
    val pass = this.skillEffects.filterIsInstance<PassEffect>()
    val obstruct = this.skillEffects.filterIsInstance<ObstructEffect>()
    val teleport = this.skillEffects.filterIsInstance<TeleportEffect>()
    val guidance = this.skillEffects.filterIsInstance<GuidanceEffect>()

    val effectOnFoe = this.skillEffects.filterIsInstance<EffectOnFoe>()

    val inCombatSupport = this.skillEffects.filterIsInstance<InCombatSupport>()
    val healEffect = this.skillEffects.filterIsInstance<HealEffect>()
    val assistEffect = this.skillEffects.filterIsInstance<AssistEffect>()

    val skillEffectSeq = skillEffects.asSequence()

    inline fun <reified R : SkillEffect> get(): Sequence<R> {
        return skillEffectSeq.filterIsInstance<R>()
    }
}