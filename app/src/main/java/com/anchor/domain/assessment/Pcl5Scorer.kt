package com.anchor.domain.assessment

/**
 * PCL-5 scoring logic.
 *
 * The scorer is a pure-Kotlin object with no Android dependencies. It accepts
 * a list of [Pcl5Response] objects and produces a [Pcl5Result] that is either
 * [Pcl5Result.Complete] or [Pcl5Result.Incomplete].
 *
 * **Screening-only note:** The PCL-5 is a screening instrument, not a
 * diagnostic instrument. A high total score indicates that further
 * clinical evaluation is advisable; it does not confirm or deny PTSD.
 */

/**
 * A single response to a PCL-5 item.
 *
 * @property itemNumber The PCL-5 item number (1-20).
 * @property score The rating for this item (0-4).
 */
data class Pcl5Response(
    val itemNumber: Int,
    val score: Int
)

/**
 * The outcome of scoring a set of PCL-5 responses.
 *
 * If enough valid responses were provided, [Complete] contains the total
 * score, per-cluster totals, and whether the provisional cutoff was met.
 * If insufficient valid responses were provided, [Incomplete] indicates
 * how many valid responses were received.
 */
sealed interface Pcl5Result {

    /**
     * All 20 items scored with valid responses.
     *
     * @property total The sum of all 20 item scores (0-80).
     * @property clusterTotals Score totals broken down by [Pcl5Cluster].
     * @property meetsProvisionalCutoff `true` if [total] >= [Pcl5Catalog.CUTOFF].
     */
    data class Complete(
        val total: Int,
        val clusterTotals: Map<Pcl5Cluster, Int>,
        val meetsProvisionalCutoff: Boolean
    ) : Pcl5Result

    /**
     * Fewer than 20 valid, unique responses were provided.
     *
     * @property answeredCount The number of valid unique responses received (0-19).
     */
    data class Incomplete(
        val answeredCount: Int
    ) : Pcl5Result
}

/**
 * Scores a set of PCL-5 responses according to the PCL-5 manual rules.
 *
 * **Validation rules:**
 * - Responses with [Pcl5Response.score] outside 0..4 are dropped (ignored).
 * - Responses with [Pcl5Response.itemNumber] outside 1..20 are dropped (ignored).
 * - Duplicate [Pcl5Response.itemNumber] values are deduplicated by keeping
 *   only the **first** occurrence; later duplicates are ignored.
 * - If fewer than 20 valid unique responses remain after filtering, an
 *   [Pcl5Result.Incomplete] is returned.
 *
 * **Scoring:**
 * - Total = sum of all 20 item scores.
 * - Cluster totals are the sum of scores within each [Pcl5Cluster].
 * - [Pcl5Result.Complete.meetsProvisionalCutoff] is `true` when total >= [Pcl5Catalog.CUTOFF].
 *
 * @param responses The raw list of item responses.
 * @return A [Pcl5Result.Complete] if 20 valid unique responses exist,
 *         otherwise [Pcl5Result.Incomplete].
 */
object Pcl5Scorer {

    fun score(responses: List<Pcl5Response>): Pcl5Result {
        // Build a map of itemNumber -> score, keeping only first occurrence,
        // and only for valid scores (0..4) and valid item numbers (1..20).
        val validItemNumbers = (1..20).toSet()
        val deduplicated = mutableMapOf<Int, Int>()

        for (response in responses) {
            if (response.score !in 0..4) continue
            if (response.itemNumber !in validItemNumbers) continue
            // Keep only the first occurrence for each item number.
            deduplicated.putIfAbsent(response.itemNumber, response.score)
        }

        if (deduplicated.size < 20) {
            return Pcl5Result.Incomplete(answeredCount = deduplicated.size)
        }

        val total = deduplicated.values.sum()

        val clusterTotals = Pcl5Cluster.entries.associateWith { cluster ->
            Pcl5Catalog.ITEMS
                .filter { it.cluster == cluster }
                .sumOf { deduplicated[it.number] ?: 0 }
        }

        return Pcl5Result.Complete(
            total = total,
            clusterTotals = clusterTotals,
            meetsProvisionalCutoff = total >= Pcl5Catalog.CUTOFF
        )
    }
}
