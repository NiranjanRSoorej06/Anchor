package com.anchor.domain.support

/**
 * Bundled support directory for India — offline-first, phone-number-centric.
 *
 * Every entry is hand-verified against `docs/india-resources.md`. Phone numbers
 * must be re-verified before public release.
 *
 * Pure Kotlin: no Android, no Compose.
 */
object SupportDirectory {

    /** Sentinel value in [SupportEntry.regions] meaning "all of India". */
    const val NATIONWIDE = "NATIONWIDE"

    /**
     * Complete ordered list of support entries.
     *
     * Ordering follows the offline display priority from india-resources.md:
     * emergency first, then helplines in default priority, directory last.
     * [forRegion] re-orders this list based on user location.
     */
    val ALL: List<SupportEntry> = listOf(
        SupportEntry(
            id = "tele_manas",
            name = "Tele MANAS",
            phoneNumber = "14416",
            url = null,
            regions = setOf(NATIONWIDE),
            hours = "24/7",
            languages = listOf(
                "Hindi", "English", "Tamil", "Telugu", "Bengali",
                "Marathi", "Kannada", "Malayalam", "Gujarati", "Punjabi",
                "Odia", "Assamese", "Manipuri", "Nepali", "Sindhi",
                "Dogri", "Kashmiri", "Maithili", "Santali", "Bodo",
            ),
            onlineOnly = false,
            category = SupportCategory.CRISIS_HELPLINE,
        ),
        SupportEntry(
            id = "emergency_112",
            name = "Emergency 112",
            phoneNumber = "112",
            url = null,
            regions = setOf(NATIONWIDE),
            hours = "24/7",
            languages = emptyList(),
            onlineOnly = false,
            category = SupportCategory.EMERGENCY,
        ),
        SupportEntry(
            id = "vandrevala_foundation",
            name = "Vandrevala Foundation",
            phoneNumber = "9999666555",
            url = null,
            regions = setOf(NATIONWIDE),
            hours = "24/7",
            languages = listOf(
                "Hindi", "English", "Tamil", "Telugu", "Bengali",
                "Marathi", "Kannada", "Malayalam", "Gujarati", "Punjabi",
                "Kashmiri",
            ),
            onlineOnly = false,
            category = SupportCategory.CRISIS_HELPLINE,
        ),
        SupportEntry(
            id = "nimhans_helpline",
            name = "NIMHANS Helpline",
            phoneNumber = "080-46110007",
            url = null,
            regions = setOf("Karnataka"),
            hours = "24/7",
            languages = listOf("English", "Kannada", "Hindi"),
            onlineOnly = false,
            category = SupportCategory.CRISIS_HELPLINE,
        ),
        SupportEntry(
            id = "icall_tiss",
            name = "iCall (TISS)",
            phoneNumber = "9152987821",
            url = null,
            regions = setOf(NATIONWIDE),
            hours = "Mon-Sat 10am-8pm",
            languages = listOf("Hindi", "English", "Marathi", "Tamil", "Bengali"),
            onlineOnly = false,
            category = SupportCategory.CRISIS_HELPLINE,
        ),
        SupportEntry(
            id = "sneha_chennai",
            name = "Sneha",
            phoneNumber = "044-24640050",
            url = null,
            regions = setOf("Tamil Nadu"),
            hours = "24/7",
            languages = listOf("English", "Tamil"),
            onlineOnly = false,
            category = SupportCategory.CRISIS_HELPLINE,
        ),
        SupportEntry(
            id = "kashmir_lifeline",
            name = "Kashmir Lifeline",
            phoneNumber = "18001807020",
            url = null,
            regions = setOf("Jammu and Kashmir"),
            hours = "24/7",
            languages = listOf("Kashmiri", "English", "Hindi", "Urdu"),
            onlineOnly = false,
            category = SupportCategory.CRISIS_HELPLINE,
        ),
        SupportEntry(
            id = "aasra",
            name = "AASRA",
            phoneNumber = "+91-9820466726",
            url = null,
            regions = setOf(NATIONWIDE),
            hours = "24/7",
            languages = listOf("Hindi", "English"),
            onlineOnly = false,
            category = SupportCategory.CRISIS_HELPLINE,
        ),
        SupportEntry(
            id = "therapist_directory",
            name = "Find a Therapist",
            phoneNumber = null,
            url = "https://therapists.therapymantra.co/therapist/india/specialization/trauma-and-ptsd",
            regions = setOf(NATIONWIDE),
            hours = "Online directory",
            languages = emptyList(),
            onlineOnly = true,
            category = SupportCategory.THERAPIST_DIRECTORY,
        ),
    )

    /**
     * Returns [SupportEntry]s ordered by relevance to [stateQuery].
     *
     * **Ordering rules** (documented with examples):
     *
     * 1. Entries whose [SupportEntry.regions] contain [stateQuery] appear
     *    first, preserving their relative order from [ALL].
     *    *Example:* `forRegion("Tamil Nadu")` → Sneha (Tamil Nadu match)
     *    is the first helpline, before any NATIONWIDE entry.
     *
     * 2. All remaining NATIONWIDE entries follow, preserving [ALL] order.
     *    *Example:* after Sneha come Tele MANAS, Emergency 112, Vandrevala,
     *    iCall, AASRA — all NATIONWIDE — in their original [ALL] order.
     *
     * 3. The online-only therapist directory entry is always last.
     *    *Example:* regardless of query, "Find a Therapist" is the final
     *    item — it requires internet and is not a crisis line.
     *
     * 4. If [stateQuery] is `null`, blank, or does not match any entry's
     *    region, only NATIONWIDE entries are returned (in [ALL] order),
     *    followed by the therapist directory.
     *    *Example:* `forRegion(null)` → Tele MANAS, Emergency 112,
     *    Vandrevala, iCall, AASRA, then Find a Therapist.
     *
     * @param stateQuery Full state/UT name (e.g. `"Tamil Nadu"`) or `null`
     *   for unfiltered nationwide view.
     * @return Ordered list; never empty (at minimum contains NATIONWIDE
     *   entries and the therapist directory).
     */
    fun forRegion(stateQuery: String?): List<SupportEntry> {
        if (stateQuery.isNullOrBlank()) {
            return ALL
        }

        val query = stateQuery.trim()

        val regional = ALL.filter { it.regions.contains(query) }
        if (regional.isEmpty()) return ALL
        val nationwide = ALL.filter { it.regions.contains(NATIONWIDE) && it.category != SupportCategory.THERAPIST_DIRECTORY }
        val otherRegionals = ALL.filter { !it.regions.contains(query) && !it.regions.contains(NATIONWIDE) }
        val directory = ALL.filter { it.category == SupportCategory.THERAPIST_DIRECTORY }

        val seen = mutableSetOf<String>()
        val result = mutableListOf<SupportEntry>()

        for (entry in regional) {
            if (seen.add(entry.id)) result += entry
        }
        for (entry in nationwide) {
            if (seen.add(entry.id)) result += entry
        }
        for (entry in otherRegionals) {
            if (seen.add(entry.id)) result += entry
        }
        for (entry in directory) {
            if (seen.add(entry.id)) result += entry
        }

        return if (result.isEmpty()) ALL else result
    }
}
