package com.vardely.app

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent

class VardelyAccessibilityService : AccessibilityService() {
    private lateinit var store: BlocklistStore
    override fun onServiceConnected() { super.onServiceConnected(); store = BlocklistStore(this) }
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return
        val target = event.packageName?.toString() ?: return
        if (target == packageName || !store.isBlockingEnabled()) return
        if (store.blockedPackages().contains(target)) {
            store.recordBlockedAttempt()
            performGlobalAction(GLOBAL_ACTION_HOME)
            return
        }
        if (store.lockedPackages().contains(target) && !store.isTemporarilyUnlocked(target)) {
            startActivity(Intent(this, AppLockActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                putExtra(AppLockActivity.EXTRA_PACKAGE, target)
            })
        }
    }
    override fun onInterrupt() = Unit
}
