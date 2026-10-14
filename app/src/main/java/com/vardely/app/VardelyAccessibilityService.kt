package com.vardely.app

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent

class VardelyAccessibilityService : AccessibilityService() {
    private lateinit var blocklistStore: BlocklistStore

    override fun onServiceConnected() {
        super.onServiceConnected()
        blocklistStore = BlocklistStore(this)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return

        val targetPackage = event.packageName?.toString() ?: return
        if (targetPackage == packageName) return
        if (blocklistStore.blockedPackages().contains(targetPackage)) {
            performGlobalAction(GLOBAL_ACTION_HOME)
        }
    }

    override fun onInterrupt() = Unit
}
