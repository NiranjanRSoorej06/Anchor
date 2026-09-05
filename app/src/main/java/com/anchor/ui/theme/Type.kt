package com.anchor.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp
import com.anchor.R

/**
 * DM Sans / DM Mono / Newsreader, resolved on-device via the Google Fonts
 * provider (Play services' font cache) — no bundled font binaries shipped
 * in the APK. Compose falls back to the platform's default font while a
 * family resolves, or permanently if the provider is unavailable (no Play
 * services, or offline on first use) — a cosmetic degrade only, never a
 * crash or blocked flow. Certificates: `res/values/font_certs.xml`.
 */
private val googleFontProvider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

private fun googleFontFamily(name: String, vararg weights: FontWeight) = FontFamily(
    weights.map { weight -> Font(googleFont = GoogleFont(name), fontProvider = googleFontProvider, weight = weight) }
)

/** Body/UI text — the design canvas's primary typeface. */
val DmSansFamily = googleFontFamily("DM Sans", FontWeight.Normal, FontWeight.Medium, FontWeight.Bold)

/** Uppercase, letter-spaced meta/status labels (e.g. "VIBRATION ON"). */
val DmMonoFamily = googleFontFamily("DM Mono", FontWeight.Normal, FontWeight.Medium)

/** The one italic emotional/quote line each acute-support screen shows. */
private val NewsreaderItalicFamily = FontFamily(
    Font(googleFont = GoogleFont("Newsreader"), fontProvider = googleFontProvider, weight = FontWeight.Light, style = FontStyle.Italic),
    Font(googleFont = GoogleFont("Newsreader"), fontProvider = googleFontProvider, weight = FontWeight.Normal, style = FontStyle.Italic)
)

val AnchorTypography = Typography(
    headlineMedium = TextStyle(fontFamily = DmSansFamily, fontWeight = FontWeight.Normal, fontSize = 28.sp, letterSpacing = (-0.3).sp),
    headlineSmall = TextStyle(fontFamily = DmSansFamily, fontWeight = FontWeight.Normal, fontSize = 24.sp),
    titleLarge = TextStyle(fontFamily = DmSansFamily, fontWeight = FontWeight.Normal, fontSize = 22.sp),
    titleMedium = TextStyle(fontFamily = DmSansFamily, fontWeight = FontWeight.Medium, fontSize = 18.sp),
    titleSmall = TextStyle(fontFamily = DmSansFamily, fontWeight = FontWeight.Medium, fontSize = 15.sp),
    bodyLarge = TextStyle(fontFamily = DmSansFamily, fontWeight = FontWeight.Normal, fontSize = 16.sp),
    bodyMedium = TextStyle(fontFamily = DmSansFamily, fontWeight = FontWeight.Normal, fontSize = 14.sp),
    bodySmall = TextStyle(fontFamily = DmSansFamily, fontWeight = FontWeight.Normal, fontSize = 13.sp),
    labelLarge = TextStyle(fontFamily = DmSansFamily, fontWeight = FontWeight.Medium, fontSize = 15.sp),
    labelMedium = TextStyle(fontFamily = DmSansFamily, fontWeight = FontWeight.Medium, fontSize = 13.sp),
    labelSmall = TextStyle(fontFamily = DmMonoFamily, fontWeight = FontWeight.Medium, fontSize = 11.sp, letterSpacing = 1.4.sp)
)

/**
 * Not a stock [Typography] role — Material3 roles don't carry per-role
 * italic semantics, and this treatment is used for exactly one line per
 * screen (e.g. "You are in a safe place", "You're here. That's enough.").
 */
val AnchorQuoteStyle = TextStyle(
    fontFamily = NewsreaderItalicFamily,
    fontStyle = FontStyle.Italic,
    fontWeight = FontWeight.Light,
    fontSize = 22.sp,
    lineHeight = 30.sp
)
