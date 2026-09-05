package com.anchor.core.companion

/** A plain lat/lon snapshot — keeps `android.location.Location` out of
 * everything downstream of the lookup so message composition stays a
 * pure, unit-testable function. */
data class LocationSnapshot(val latitude: Double, val longitude: Double)

/**
 * Pure text composition for the Companion Mode alert — appends the
 * last-known coordinates as plain numbers when a location is available,
 * leaves the base message untouched otherwise. Kept separate from
 * [CompanionNotificationEngine] (which is Android-API-bound and hard to
 * unit test) so this logic gets real test coverage.
 *
 * **Why plain numbers, not a `maps.google.com` link:** verified on-device
 * (screenshot from the user, 2026-09-06) — a plain-text alert with no link
 * sent successfully, while every alert carrying a `https://maps.google.com/...`
 * link showed "Not sent" in the Messages app, consistently. Indian
 * carriers commonly reject SMS containing URLs from senders that aren't
 * DLT-registered (a TRAI anti-spam/phishing rule) — an app calling
 * `SmsManager` directly hits this even though a human typing the same
 * link in the Messages app might not. Coordinates alone read as plain
 * digits, not a link, and the recipient can still paste them into any
 * maps app.
 */
object CompanionMessageComposer {

    fun withLocation(baseMessage: String, location: LocationSnapshot?): String {
        if (location == null) return baseMessage
        return "$baseMessage\nLocation: ${location.latitude}, ${location.longitude} (paste into Maps)"
    }

    /**
     * Prepends India's country code to a bare 10-digit mobile number.
     * `SmsManager.sendTextMessage`'s destination address has been observed
     * to fail with `RESULT_ERROR_GENERIC_FAILURE` on some carrier/
     * subscription combinations for a country-code-less number, even when
     * the same number sends fine from the stock Messages app — this app's
     * contacts are entered as plain local numbers (`CompanionModeScreen`'s
     * "Phone Number" field has no country picker), so normalize here
     * rather than push a country-code requirement onto the user. Numbers
     * that already look international (start with `+`) or aren't a
     * plausible bare 10-digit Indian mobile number are left untouched.
     */
    fun normalizePhoneNumber(raw: String): String {
        val trimmed = raw.trim()
        return if (!trimmed.startsWith("+") && trimmed.length == 10 && trimmed.all(Char::isDigit)) {
            "+91$trimmed"
        } else {
            trimmed
        }
    }
}
