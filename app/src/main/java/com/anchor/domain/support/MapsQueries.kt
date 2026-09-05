package com.anchor.domain.support

/**
 * Preset Maps queries and URI builder for the "You are not alone" section.
 *
 * Generates `geo:0,0?q=` URIs compatible with Android's `ACTION_VIEW`
 * intent. Full `Uri.encode` should be applied in the UI layer before
 * constructing the intent — this object only handles minimal
 * space/comma encoding required for well-formed preset strings.
 *
 * @see content-sources.md §"You are not alone" section
 */
object MapsQueries {

    /** Geo URI prefix for a free-text query via the default maps app. */
    const val BASE = "geo:0,0?q="

    /** Preset: nearest mental-health clinic. */
    const val CLINIC = "mental health clinic near me"

    /** Preset: nearest psychiatrist. */
    const val PSYCHIATRIST = "psychiatrist near me"

    /** Preset: nearest counselling service. */
    const val COUNSELLING = "counselling services near me"

    /**
     * Builds a minimal geo-URI for [query].
     *
     * Performs lightweight encoding only: spaces become `+` and commas
     * become `%2C`. Full `Uri.encode` (which handles all reserved
     * characters) should be applied by the UI layer before passing the
     * result to an `ACTION_VIEW` intent.
     *
     * @param query free-text search string (e.g. [CLINIC]).
     * @return a `geo:0,0?q=…` URI string ready for intent construction.
     */
    fun uriFor(query: String): String =
        BASE + query.replace(" ", "+").replace(",", "%2C")
}
