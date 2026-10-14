package com.vardely.app

import android.content.Context

class BlocklistStore(context: Context) {
    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    fun blockedPackages(): Set<String> =
        preferences.getStringSet(BLOCKED_PACKAGES_KEY, emptySet()).orEmpty()

    fun setBlockedPackages(packages: Set<String>) {
        preferences.edit().putStringSet(BLOCKED_PACKAGES_KEY, packages.toSet()).apply()
    }

    companion object {
        private const val PREFERENCES_NAME = "vardely_preferences"
        private const val BLOCKED_PACKAGES_KEY = "blocked_packages"
    }
}
