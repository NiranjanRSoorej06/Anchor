package com.anchor.domain.support

/**
 * Categorises support entries by their urgency and purpose.
 *
 * Ordering in the enum is intentional — it mirrors the priority tiers
 * described in `docs/india-resources.md`:
 * 1. [EMERGENCY]  — immediate-danger numbers (112).
 * 2. [CRISIS_HELPLINE] — 24/7 or scheduled phone counselling lines.
 * 3. [THERAPIST_DIRECTORY] — long-term, online-only directories.
 */
enum class SupportCategory {
    /** Immediate-danger / life-threatening emergency number. */
    EMERGENCY,
    /** 24/7 or scheduled phone-based crisis counselling line. */
    CRISIS_HELPLINE,
    /** Long-term therapy directory (requires internet). */
    THERAPIST_DIRECTORY,
}

/**
 * A single entry in the bundled India support directory.
 *
 * Every field is documented because callers (UI, accessibility, offline cache)
 * depend on nullable-vs-non-null guarantees — phone entries always carry a
 * [phoneNumber]; the therapist directory entry always carries a [url] and
 * sets [onlineOnly] to `true`.
 *
 * ## Region conventions
 * - [regions] uses full Indian state/UT names
 *   (e.g. `"Tamil Nadu"`, `"Jammu and Kashmir"`, `"Karnataka"`).
 * - National entries that work everywhere use the sentinel
 *   [SupportDirectory.NATIONWIDE] (`"NATIONWIDE"`).
 *
 * @property id Stable, machine-readable identifier (snake_case).
 * @property name Human-readable display name.
 * @property phoneNumber Primary phone number. Non-null for every phone
 *   helpline; `null` for the online-only therapist directory entry.
 * @property url Web URL for entries that provide an online resource.
 *   Non-null only for the therapist directory entry.
 * @property regions Set of region strings the entry is most relevant to.
 *   Never empty. Contains [SupportDirectory.NATIONWIDE] for national lines.
 * @property hours Human-readable availability window, e.g. `"24/7"` or
 *   `"Mon-Sat 10am-8pm"`.
 * @property languages Languages supported by this helpline (ISO-like names).
 *   Empty list for entries without explicit language data.
 * @property onlineOnly `true` when the entry requires internet access
 *   (therapist directory). `false` for all phone helplines.
 * @property category [SupportCategory] tier used for UI grouping and
 *   default sort priority.
 */
data class SupportEntry(
    val id: String,
    val name: String,
    val phoneNumber: String?,
    val url: String?,
    val regions: Set<String>,
    val hours: String,
    val languages: List<String>,
    val onlineOnly: Boolean,
    val category: SupportCategory,
)
