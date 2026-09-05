package com.anchor.ui.theme

/**
 * The three palette directions reviewed as a design canvas before this was
 * written (Nord/Arctic, Soft Sage, Warm Sand). None of these is claimed to
 * be clinically "soothing" — that isn't an evidenced claim anyone can make
 * (see evidence.md's standard for claim strength). This is an ordinary
 * design choice, offered to the user because taste varies.
 *
 * [NORD] is the default per team decision.
 */
enum class ThemeVariant(val label: String) {
    NORD("Nord"),
    SAGE("Soft Sage"),
    SAND("Warm Sand")
}
