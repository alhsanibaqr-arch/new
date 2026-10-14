package com.vardely.app

import android.content.Context
import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

class BlocklistStore(context: Context) {
    private val preferences = context.getSharedPreferences("vardely_preferences", Context.MODE_PRIVATE)
    fun blockedPackages(): Set<String> = preferences.getStringSet("blocked_packages", emptySet()).orEmpty().toSet()
    fun setAppBlocked(packageName: String, blocked: Boolean) {
        val updated = blockedPackages().toMutableSet()
        if (blocked) updated.add(packageName) else updated.remove(packageName)
        preferences.edit().putStringSet("blocked_packages", updated).apply()
    }
    fun lockedPackages(): Set<String> = preferences.getStringSet("locked_packages", emptySet()).orEmpty().toSet()
    fun setAppLocked(packageName: String, locked: Boolean) {
        val updated = lockedPackages().toMutableSet()
        if (locked) updated.add(packageName) else updated.remove(packageName)
        preferences.edit().putStringSet("locked_packages", updated).apply()
    }
    fun isBlockingEnabled() = preferences.getBoolean("blocking_enabled", false)
    fun setBlockingEnabled(enabled: Boolean) = preferences.edit().putBoolean("blocking_enabled", enabled).apply()
    fun setDisclosureAccepted(accepted: Boolean) = preferences.edit().putBoolean("accessibility_disclosure_accepted", accepted).apply()
    fun hasPin() = preferences.contains("pin_hash") && preferences.contains("pin_salt")
    fun setPin(pin: String) {
        val salt = ByteArray(16).also { SecureRandom().nextBytes(it) }
        val hash = derivePinHash(pin, salt)
        preferences.edit()
            .putString("pin_salt", Base64.encodeToString(salt, Base64.NO_WRAP))
            .putString("pin_hash", Base64.encodeToString(hash, Base64.NO_WRAP))
            .apply()
    }
    fun verifyPin(pin: String): Boolean {
        val salt = preferences.getString("pin_salt", null)?.let { Base64.decode(it, Base64.NO_WRAP) } ?: return false
        val expected = preferences.getString("pin_hash", null)?.let { Base64.decode(it, Base64.NO_WRAP) } ?: return false
        return MessageDigest.isEqual(expected, derivePinHash(pin, salt))
    }
    fun allowPackageTemporarily(packageName: String, durationMillis: Long = 120_000L) {
        preferences.edit().putLong("unlock_until_$packageName", System.currentTimeMillis() + durationMillis).apply()
    }
    fun isTemporarilyUnlocked(packageName: String): Boolean =
        System.currentTimeMillis() < preferences.getLong("unlock_until_$packageName", 0L)
    fun recordBlockedAttempt(date: String = java.time.LocalDate.now().toString()) {
        val key = "blocked_attempts_$date"
        preferences.edit().putInt(key, preferences.getInt(key, 0) + 1).apply()
    }
    fun attemptsForDate(date: String) = preferences.getInt("blocked_attempts_$date", 0)

    private fun derivePinHash(pin: String, salt: ByteArray): ByteArray {
        val spec = PBEKeySpec(pin.toCharArray(), salt, 120_000, 256)
        return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded
    }
}
