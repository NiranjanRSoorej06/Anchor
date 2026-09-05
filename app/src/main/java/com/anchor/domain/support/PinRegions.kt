package com.anchor.domain.support

/**
 * Maps the first two digits of an Indian PIN code to the corresponding
 * state or union territory (or special region such as Army Postal Service).
 *
 * The PIN (Postal Index Number) is a 6-digit code where the first digit
 * denotes a postal zone (1–8 geographical, 9 = Army) and the first two
 * digits narrow to a sub-region / postal circle / state.
 *
 * This table is transcribed from `docs/ptsd-care-india-map.md` §1.2
 * and covers all 26 range-rows found in the official India Post
 * structure.
 *
 * **Contract with [SupportDirectory]:** Where a regional entry exists in
 * [SupportDirectory], the `state` values in this table SHOULD exactly
 * match the region keys used by [SupportDirectory.forRegion].  Grouped
 * labels that intentionally span multiple states (e.g.
 * "Uttar Pradesh and Uttarakhand") have no single matching directory
 * entry and therefore fall back to the nationwide list — this is by
 * design.
 *
 * @property prefix2 Exactly two digits (e.g. `"11"`, `"56"`).
 * @property state  Human-readable state/UT/region name (e.g. `"Delhi"`,
 *   `"Karnataka"`). Grouped prefixes use a combined label when the
 *   prefix spans more than one state/UT.
 */
data class PinRegion(val prefix2: String, val state: String)

/**
 * Offline lookup table: first two digits of an Indian PIN → state/UT.
 *
 * Covers all prefixes documented in `docs/ptsd-care-india-map.md` §1.2.
 * Gaps in the sequence (e.g. 00–10, 29, 35, 54–55, 65–66, 86–89) are
 * intentionally absent — any PIN beginning with an unmapped prefix
 * returns `null` from [stateForPincode].
 *
 * **Usage example**:
 * ```
 * val region = PinRegions.stateForPincode("560001")  // → "Karnataka"
 * val region = PinRegions.stateForPincode("110001")  // → "Delhi"
 * val region = PinRegions.stateForPincode("000000")  // → null (unmapped)
 * ```
 */
object PinRegions {

    /** Complete list of all known PIN-prefix → state/UT mappings. */
    val ALL: List<PinRegion> = listOf(
        // ── Zone 1: North ─────────────────────────────────────────────
        PinRegion("11", "Delhi"),
        PinRegion("12", "Haryana"),
        PinRegion("13", "Haryana"),
        PinRegion("14", "Punjab and Chandigarh"),
        PinRegion("15", "Punjab and Chandigarh"),
        PinRegion("16", "Punjab and Chandigarh"),
        PinRegion("17", "Himachal Pradesh"),
        PinRegion("18", "Jammu and Kashmir"),
        PinRegion("19", "Jammu and Kashmir"),

        // ── Zone 2: North ─────────────────────────────────────────────
        PinRegion("20", "Uttar Pradesh"),
        PinRegion("21", "Uttar Pradesh"),
        PinRegion("22", "Uttar Pradesh"),
        PinRegion("23", "Uttar Pradesh and Uttarakhand"),
        PinRegion("24", "Uttar Pradesh and Uttarakhand"),
        PinRegion("25", "Uttar Pradesh and Uttarakhand"),
        PinRegion("26", "Uttar Pradesh and Uttarakhand"),
        PinRegion("27", "Uttar Pradesh and Uttarakhand"),
        PinRegion("28", "Uttar Pradesh and Uttarakhand"),

        // ── Zone 3: West ──────────────────────────────────────────────
        PinRegion("30", "Rajasthan"),
        PinRegion("31", "Rajasthan"),
        PinRegion("32", "Rajasthan"),
        PinRegion("33", "Rajasthan"),
        PinRegion("34", "Rajasthan"),
        PinRegion("36", "Gujarat"),
        PinRegion("37", "Gujarat"),
        PinRegion("38", "Gujarat"),
        PinRegion("39", "Gujarat and Dadra and Nagar Haveli"),

        // ── Zone 4: West ──────────────────────────────────────────────
        PinRegion("40", "Maharashtra"),
        PinRegion("41", "Maharashtra"),
        PinRegion("42", "Maharashtra"),
        PinRegion("43", "Maharashtra and Madhya Pradesh"),
        PinRegion("44", "Maharashtra and Madhya Pradesh"),
        PinRegion("45", "Madhya Pradesh"),
        PinRegion("46", "Madhya Pradesh"),
        PinRegion("47", "Madhya Pradesh"),
        PinRegion("48", "Madhya Pradesh"),
        PinRegion("49", "Chhattisgarh"),

        // ── Zone 5: South ─────────────────────────────────────────────
        PinRegion("50", "Telangana"),
        PinRegion("51", "Andhra Pradesh"),
        PinRegion("52", "Andhra Pradesh"),
        PinRegion("53", "Andhra Pradesh"),
        PinRegion("56", "Karnataka"),
        PinRegion("57", "Karnataka"),
        PinRegion("58", "Karnataka"),
        PinRegion("59", "Karnataka"),

        // ── Zone 6: South ─────────────────────────────────────────────
        PinRegion("60", "Tamil Nadu"),
        PinRegion("61", "Tamil Nadu"),
        PinRegion("62", "Tamil Nadu"),
        PinRegion("63", "Tamil Nadu"),
        PinRegion("64", "Tamil Nadu"),
        PinRegion("67", "Kerala"),
        PinRegion("68", "Kerala"),
        PinRegion("69", "Kerala"),

        // ── Zone 7: East ──────────────────────────────────────────────
        PinRegion("70", "West Bengal"),
        PinRegion("71", "West Bengal"),
        PinRegion("72", "West Bengal"),
        PinRegion("73", "West Bengal"),
        PinRegion("74", "West Bengal"),
        PinRegion("75", "Odisha"),
        PinRegion("76", "Odisha"),
        PinRegion("77", "Odisha"),
        PinRegion("78", "Assam and Northeast States"),
        PinRegion("79", "Northeast States"),

        // ── Zone 8: East ──────────────────────────────────────────────
        PinRegion("80", "Bihar"),
        PinRegion("81", "Bihar"),
        PinRegion("82", "Bihar"),
        PinRegion("83", "Bihar"),
        PinRegion("84", "Jharkhand"),
        PinRegion("85", "Jharkhand"),

        // ── Zone 9: Military ──────────────────────────────────────────
        PinRegion("90", "Army Postal Service"),
        PinRegion("91", "Army Postal Service"),
        PinRegion("92", "Army Postal Service"),
        PinRegion("93", "Army Postal Service"),
        PinRegion("94", "Army Postal Service"),
        PinRegion("95", "Army Postal Service"),
        PinRegion("96", "Army Postal Service"),
        PinRegion("97", "Army Postal Service"),
        PinRegion("98", "Army Postal Service"),
        PinRegion("99", "Army Postal Service"),
    )

    /**
     * Returns the state/UT/region name for [pincode], or `null` if the
     * input is invalid or the first two digits are not in this table.
     *
     * **Validation rules**:
     * - [pincode] is trimmed of leading/trailing whitespace.
     * - After trimming, must be exactly 6 ASCII digits (`0`–`9`).
     * - The first two digits are looked up in [ALL]; if absent, `null`
     *   is returned.
     *
     * **Examples**:
     * ```
     * PinRegions.stateForPincode("110001")  // → "Delhi"
     * PinRegions.stateForPincode(" 560001 ") // → "Karnataka" (whitespace trimmed)
     * PinRegions.stateForPincode("600001")  // → "Tamil Nadu"
     * PinRegions.stateForPincode("781001")  // → "Assam and Northeast States"
     * PinRegions.stateForPincode("194101")  // → "Jammu and Kashmir"
     * PinRegions.stateForPincode("900001")  // → "Army Postal Service"
     * PinRegions.stateForPincode("54321")   // → null (5 digits)
     * PinRegions.stateForPincode("abcdef")   // → null (letters)
     * PinRegions.stateForPincode("")          // → null (blank)
     * PinRegions.stateForPincode("000001")   // → null (prefix 00 unmapped)
     * ```
     *
     * @param pincode User-entered PIN code string.
     * @return State/UT/region name, or `null` for invalid/unmapped input.
     */
    fun stateForPincode(pincode: String?): String? {
        val trimmed = pincode?.trim() ?: return null
        if (trimmed.length != 6) return null
        if (!trimmed.all { it in '0'..'9' }) return null
        val prefix = trimmed.substring(0, 2)
        return ALL.firstOrNull { it.prefix2 == prefix }?.state
    }
}
