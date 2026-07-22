package com.rafiq.util

import com.rafiq.domain.model.User
import com.rafiq.domain.model.PersonalityMatrix
import kotlin.math.abs

object CompatibilityCalculator {

    /**
     * Calculates compatibility score (0 to 100%) between two users based on:
     * 1. Personality Matrix traits overlap (lifestyle, intellect, ambition, social, values)
     * 2. Relationship Goal alignment
     * 3. Religion alignment
     * 4. Shared Hobbies intersection ratio
     */
    fun calculateScore(me: User?, other: User): Int {
        if (me == null) return (80..95).random()

        var score = 60.0 // Base score

        // 1. Personality Matrix (up to +20 points)
        val p1 = me.personality
        val p2 = other.personality
        val diff = (
            abs(p1.lifestyle - p2.lifestyle) +
            abs(p1.intellect - p2.intellect) +
            abs(p1.ambition - p2.ambition) +
            abs(p1.social - p2.social) +
            abs(p1.values - p2.values)
        ) / 5.0
        val personalityBonus = (1.0 - diff) * 20.0
        score += personalityBonus

        // 2. Relationship Goal (up to +10 points)
        if (me.relationshipGoal == other.relationshipGoal) {
            score += 10.0
        }

        // 3. Religion (up to +5 points)
        if (me.religion == other.religion) {
            score += 5.0
        }

        // 4. Shared Hobbies (up to +5 points)
        val commonHobbies = me.hobbies.intersect(other.hobbies.toSet()).size
        score += (commonHobbies * 2.5).coerceAtMost(5.0)

        return score.toInt().coerceIn(65, 99)
    }
}
