package com.anchor.core.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Handler
import android.os.Looper
import androidx.core.content.ContextCompat
import com.anchor.core.companion.LocationSnapshot
import com.google.android.gms.location.LocationServices

/**
 * Best-effort location lookup for the Companion Mode SOS alert.
 *
 * **Why this went through a rewrite:** the original version only checked
 * [android.location.LocationManager]'s classic providers (`"gps"`,
 * `"network"`, `"passive"`). On a real test device this was verified
 * empirically to always return null — `adb shell dumpsys location` showed
 * a fresh cached fix, but only under the **fused** provider, which belongs
 * to Google Play Services' `FusedLocationProviderClient`, a separate API
 * `LocationManager.getAllProviders()` does not expose. Virtually every
 * modern Android device with Play Services routes location this way, so
 * fused is now tried first; the old `LocationManager` scan remains as a
 * fallback for devices without Play Services.
 *
 * Never requests a fresh GPS fix (no `requestSingleUpdate`/exact
 * `getCurrentLocation`) — the SMS alert must never block or meaningfully
 * delay on GPS acquisition. [getAsync] always calls [onResult] on the
 * calling thread's looper, bounded by [FUSED_TIMEOUT_MS] so a slow or
 * hung Play Services call can't hold up the alert either.
 */
object LastKnownLocationProvider {

    private const val FUSED_TIMEOUT_MS = 2500L

    /**
     * Looks up the best available last-known location and delivers it to
     * [onResult] — `null` if nothing is cached anywhere or permission
     * isn't granted. Never throws, never blocks.
     */
    fun getAsync(context: Context, onResult: (LocationSnapshot?) -> Unit) {
        if (!hasLocationPermission(context)) {
            onResult(null)
            return
        }

        val handler = Handler(Looper.getMainLooper())
        var completed = false
        fun complete(result: LocationSnapshot?) {
            if (completed) return
            completed = true
            handler.removeCallbacksAndMessages(null)
            onResult(result)
        }

        try {
            LocationServices.getFusedLocationProviderClient(context).lastLocation
                .addOnSuccessListener { location ->
                    complete(location?.let { LocationSnapshot(it.latitude, it.longitude) } ?: legacyLastKnown(context))
                }
                .addOnFailureListener { complete(legacyLastKnown(context)) }
        } catch (e: Exception) {
            // Play Services unavailable/misconfigured on this device — fall
            // back rather than losing the location line entirely.
            complete(legacyLastKnown(context))
        }

        handler.postDelayed({ complete(legacyLastKnown(context)) }, FUSED_TIMEOUT_MS)
    }

    private fun hasLocationPermission(context: Context): Boolean {
        val hasFine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED
        val hasCoarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED
        return hasFine || hasCoarse
    }

    /** Fallback for devices without (working) Play Services. */
    private fun legacyLastKnown(context: Context): LocationSnapshot? {
        if (!hasLocationPermission(context)) return null
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return null
        return try {
            locationManager.allProviders
                .mapNotNull { provider -> runCatching { locationManager.getLastKnownLocation(provider) }.getOrNull() }
                .maxByOrNull { it.time }
                ?.let { LocationSnapshot(latitude = it.latitude, longitude = it.longitude) }
        } catch (e: SecurityException) {
            null
        }
    }
}
