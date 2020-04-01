package me.kumatheta.feh.skill

import me.kumatheta.feh.skill.effect.AssistEffect
import me.kumatheta.feh.skill.effect.DisableFoeAllySupportEffect
import me.kumatheta.feh.skill.effect.EffectOnFoe
import me.kumatheta.feh.skill.effect.GuidanceEffect
import me.kumatheta.feh.skill.effect.HealEffect
import me.kumatheta.feh.skill.effect.InCombatSkillEffect
import me.kumatheta.feh.skill.effect.InCombatSupport
import me.kumatheta.feh.skill.effect.ObstructEffect
import me.kumatheta.feh.skill.effect.PassEffect
import me.kumatheta.feh.skill.effect.PostInitiateMovement
import me.kumatheta.feh.skill.effect.SkillEffect
import me.kumatheta.feh.skill.effect.StartOfTurnEffect
import me.kumatheta.feh.skill.effect.TeleportEffect

class SkillSet(skillEffects: List<SkillEffect>) {
    private val inCombatSkillEffects = skillEffects.filterIsInstance<InCombatSkillEffect>()
    val skillEffectSeq = this.inCombatSkillEffects.asSequence()

    val postInitiateMovement: MovementEffect?

    init {
        val movements = skillEffects.filterIsInstance<PostInitiateMovement>()
        if (movements.size > 1) {
            throw IllegalStateException("more than one possible movement post combat")
        }
        postInitiateMovement = movements.singleOrNull()?.movementEffect
    }

    val startOfTurn = skillEffects.filterIsInstance<StartOfTurnEffect>()
    val pass = skillEffects.filterIsInstance<PassEffect>()
    val obstruct = skillEffects.filterIsInstance<ObstructEffect>()
    val teleport = skillEffects.filterIsInstance<TeleportEffect>()
    val guidance = skillEffects.filterIsInstance<GuidanceEffect>()
    val effectOnFoe = skillEffects.filterIsInstance<EffectOnFoe>()
    val inCombatSupport = skillEffects.filterIsInstance<InCombatSupport>()
    val healEffect = skillEffects.filterIsInstance<HealEffect>()
    val assistEffect = skillEffects.filterIsInstance<AssistEffect>()
    val disableFoeAllySupport = skillEffects.filterIsInstance<DisableFoeAllySupportEffect>()
}