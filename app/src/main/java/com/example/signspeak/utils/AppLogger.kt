package com.example.signspeak.utils

import android.util.Log

/**
 * Centralized logger for SignSpeak.
 *
 * HOW TO FILTER IN LOGCAT:
 *   Type "SignSpeak" in the Logcat search bar → see ONLY this app's logs instantly.
 *   Or filter by sub-tag, e.g. "SignSpeak:JoinClass" for join-class logs only.
 *
 * Usage:
 *   AppLogger.d("JoinClass", "Room id = $roomId")
 *   AppLogger.e("LiveClass", "Crash", exception)
 */
object AppLogger {

    private const val GLOBAL_TAG = "SignSpeak"

    // ── Verbose ──────────────────────────────────────────────────────────────
    fun v(subTag: String, msg: String) = Log.v("$GLOBAL_TAG:$subTag", msg)

    // ── Debug ────────────────────────────────────────────────────────────────
    fun d(subTag: String, msg: String) = Log.d("$GLOBAL_TAG:$subTag", msg)

    // ── Info ─────────────────────────────────────────────────────────────────
    fun i(subTag: String, msg: String) = Log.i("$GLOBAL_TAG:$subTag", msg)

    // ── Warning ──────────────────────────────────────────────────────────────
    fun w(subTag: String, msg: String, throwable: Throwable? = null) {
        if (throwable != null) Log.w("$GLOBAL_TAG:$subTag", msg, throwable)
        else Log.w("$GLOBAL_TAG:$subTag", msg)
    }

    // ── Error ────────────────────────────────────────────────────────────────
    fun e(subTag: String, msg: String, throwable: Throwable? = null) {
        if (throwable != null) Log.e("$GLOBAL_TAG:$subTag", msg, throwable)
        else Log.e("$GLOBAL_TAG:$subTag", msg)
    }

    // ── Section separator (easy to spot in logs) ─────────────────────────────
    fun section(subTag: String, title: String) {
        Log.d("$GLOBAL_TAG:$subTag", "━━━━━━━━━━ $title ━━━━━━━━━━")
    }
}
