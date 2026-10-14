package com.vardely.app

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.os.SystemClock
import android.view.accessibility.AccessibilityEvent

class VardelyAccessibilityService : AccessibilityService() {
    private lateinit var store: BlocklistStore
    private var lastLockPackage: String? = null
    private var lastLockStartedAt = 0L

    override fun onServiceConnected() {
        super.onServiceConnected()
        store = BlocklistStore(this)
        if (!store.isDisclosureAccepted()) disableSelf()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return
        val target = event.packageName?.toString() ?: return
        if (target == packageName) return

        if (store.isBlockingEnabled() && store.blockedPackages().contains(target)) {
            store.recordBlockedAttempt()
            performGlobalAction(GLOBAL_ACTION_HOME)
            return
        }

        if (store.hasPin() && store.lockedPackages().contains(target) && !store.isTemporarilyUnlocked(target)) {
            val now = SystemClock.elapsedRealtime()
            if (target == lastLockPackage && now - lastLockStartedAt < 1_000L) return
            lastLockPackage = target
            lastLockStartedAt = now
            startActivity(Intent(this, AppLockActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                putExtra(AppLockActivity.EXTRA_PACKAGE, target)
            })
        }
    }

    override fun onInterrupt() = Unit
}
