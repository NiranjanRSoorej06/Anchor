# "Support Near You" — Offline Feasibility (2026-09-05)

Question: can Anchor implement clean "find support near you" offline, no backend, hackathon scope?

## Verdict
- **Live street-level nearby clinics: NO.** Needs Google Places/Maps SDK (internet + API key + billing, no free tier since Mar 2025) or GB-scale offline OSM data. Kills the offline story.
- **Region-mapped static directory: YES, ~8h, zero internet, zero cost.** This is the honest "near you": state-level routing over the bundled helpline directory. No comparable India mental-health app does even this.

## How it works (no internet at any step)
1. User taps "Find Support Near You" → rationale dialog (location used once, never stored/shared, or pick state manually).
2. Single foreground GPS fix (AOSP LocationManager GPS_PROVIDER works fully offline; FusedLocationProvider also fixes offline via GPS). COARSE preferred; never background; never stored — in-memory only, discarded.
3. Bundled ~25KB JSON: 36 state/UT bounding boxes + centroids + state→helpline mapping → nearest-centroid/point-in-polygon lookup, fully local.
4. Display: Tele MANAS default + regional number first (Kochi→NIMHANS, Chennai→Sneha, Srinagar→Kashmir Lifeline, Delhi→Tele MANAS+Vandrevala) + full directory. Denied/unavailable → manual 36-state picker → full national directory. Never an empty screen.

## Permissions (minSdk 26, targetSdk 35)
- `ACCESS_COARSE_LOCATION` only (city-level is plenty); Android 12+ may force-coarse anyway — design for it. No `BACKGROUND_LOCATION` (one-shot foreground query). Android Geocoder is backend-dependent → unusable offline; bundled table replaces it.

## Privacy rules (location + PTSD = sensitive)
- Ask only on explicit tap, never passively. Minimal permission. Never store location. Manual picker always offered. Frame: "used once to find your state, never stored, never sent anywhere."

## Effort (~8h)
Data table 0.5h + lookup 1h + permission flow 1.5h + location fix 1h + UI 2h + integration 0.5h + testing (grant/deny/airplane/GPS-only) 1.5h.

## Sources
- LocationManager: https://developer.android.com/reference/android/location/LocationManager
- FLP: https://developers.google.com/android/reference/com/google/android/gms/location/FusedLocationProviderClient
- Permissions: https://developer.android.com/develop/sensors-and-location/location/permissions
- Background: https://developer.android.com/develop/sensors-and-location/location/background
- Geocoder (backend-dependent): https://developer.android.com/reference/android/location/Geocoder
- Places billing (no free tier): https://developers.google.com/maps/documentation/places/web-service/usage-and-billing
- OSM size discussion: https://github.com/osmdroid/osmdroid/issues/297
