package com.rafiq.util

import com.rafiq.domain.model.User

object IcebreakerGenerator {
    fun generate(userA: User, userB: User): List<String> {
        val mutualHobbies = userA.hobbies.filter { userB.hobbies.contains(it) }
        val icebreakers = mutableListOf<String>()

        if (mutualHobbies.isNotEmpty()) {
            icebreakers.add("I see we both love ${mutualHobbies.random()}! How long have you been into that?")
            icebreakers.add("Since we both like ${mutualHobbies.random()}, what's your favorite thing about it?")
        }

        if (userA.country == userB.country) {
            icebreakers.add("Nice to meet someone from ${userA.country}! Are you in the city often?")
        }

        if (userA.relationshipGoal == userB.relationshipGoal) {
            icebreakers.add("It's cool we're both looking for ${userA.relationshipGoal.name.lowercase()}!")
        }

        // Add some generic high-quality ones if list is empty
        if (icebreakers.isEmpty()) {
            icebreakers.addAll(listOf(
                "If you could travel anywhere tomorrow, where would you go?",
                "What's your favorite way to spend a weekend?",
                "What's one thing that made you smile today?",
                "Are you more of a morning person or a night owl?"
            ))
        }

        return icebreakers.shuffled()
    }
}
