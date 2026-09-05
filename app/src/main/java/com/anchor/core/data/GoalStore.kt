package com.anchor.core.data

import com.anchor.domain.goals.Goal
import com.anchor.domain.goals.GoalValidator
import com.anchor.domain.goals.ValidationResult

/**
 * Persistence contract for behavioral goals.
 *
 * Pure Kotlin: no Android, no Compose. Implementations can back this
 * with DataStore+JSON, Room, or in-memory maps.
 */
interface GoalStore {

    /** Return all saved goals in insertion order. */
    suspend fun list(): List<Goal>

    /**
     * Insert or replace a goal by its [Goal.id].
     *
     * @throws IllegalArgumentException if [GoalValidator.validate] returns
     *   [ValidationResult.Invalid].
     */
    suspend fun save(g: Goal)

    /**
     * Record a completion timestamp for the goal identified by [id].
     *
     * @param id Must reference an existing [Goal.id].
     * @param atMillis Epoch millis when the goal was completed.
     * @throws IllegalArgumentException if [id] does not reference a known goal.
     */
    suspend fun complete(id: String, atMillis: Long)
}
