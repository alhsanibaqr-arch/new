package com.vardely.app

import android.content.Context

class BlocklistStore(context: Context) {
    private val preferences = context.getSharedPreferences("vardely_preferences", Context.MODE_PRIVATE)
    fun blockedPackages(): Set<String> = preferences.getStringSet("blocked_packages", emptySet()).orEmpty().toSet()
    fun setAppBlocked(packageName: String, blocked: Boolean) {
        val updated = blockedPackages().toMutableSet()
        if (blocked) updated.add(packageName) else updated.remove(packageName)
        preferences.edit().putStringSet("blocked_packages", updated).apply()
    }
    fun isBlockingEnabled() = preferences.getBoolean("blocking_enabled", false)
    fun setBlockingEnabled(enabled: Boolean) = preferences.edit().putBoolean("blocking_enabled", enabled).apply()
    fun setDisclosureAccepted(accepted: Boolean) = preferences.edit().putBoolean("accessibility_disclosure_accepted", accepted).apply()
    fun recordBlockedAttempt(date: String = java.time.LocalDate.now().toString()) {
        val key = "blocked_attempts_$date"
        preferences.edit().putInt(key, preferences.getInt(key, 0) + 1).apply()
    }
    fun attemptsForDate(date: String) = preferences.getInt("blocked_attempts_$date", 0)
}
