package com.anchor.core.data

import com.anchor.domain.goals.Goal
import com.anchor.domain.goals.GoalValidator
import com.anchor.domain.goals.ValidationResult

/**
 * In-memory implementation of [GoalStore] for testing and previews.
 *
 * Not thread-safe — safe for single-coroutine test usage.
 */
class InMemoryGoalStore : GoalStore {

    private val goals = mutableListOf<Goal>()

    override suspend fun list(): List<Goal> = goals.toList()

    override suspend fun save(g: Goal) {
        val result = GoalValidator.validate(g)
        require(result is ValidationResult.Valid) {
            "Invalid goal: ${(result as ValidationResult.Invalid).reasons}"
        }
        goals.removeAll { it.id == g.id }
        goals.add(g)
    }

    override suspend fun complete(id: String, atMillis: Long) {
        val index = goals.indexOfFirst { it.id == id }
        require(index >= 0) { "No goal with id=$id" }
        val existing = goals[index]
        goals[index] = existing.copy(
            completionsMillis = existing.completionsMillis + atMillis
        )
    }
}
