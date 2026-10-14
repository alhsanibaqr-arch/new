package com.vardely.app

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.getSystemService

class MainActivity : AppCompatActivity() {
    private lateinit var serviceStatusText: TextView
    private lateinit var blockedAppsText: TextView
    private lateinit var blocklistStore: BlocklistStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        blocklistStore = BlocklistStore(this)
        serviceStatusText = findViewById(R.id.serviceStatusText)
        blockedAppsText = findViewById(R.id.blockedAppsText)
        findViewById<Button>(R.id.accessibilitySettingsButton).setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }
    }

    override fun onResume() {
        super.onResume()
        renderStatus()
    }

    private fun renderStatus() {
        val enabled = getSystemService<AccessibilityManager>()
            ?.getEnabledAccessibilityServiceList(AccessibilityManager.FEEDBACK_ALL_MASK)
            ?.any { info ->
                info.resolveInfo.serviceInfo.let { serviceInfo ->
                    serviceInfo.packageName == packageName &&
                        serviceInfo.name == VardelyAccessibilityService::class.java.name
                }
            } == true

        serviceStatusText.text = getString(
            R.string.service_status,
            getString(if (enabled) R.string.enabled else R.string.disabled)
        )
        blockedAppsText.text = getString(R.string.blocked_apps, blocklistStore.blockedPackages().size)
    }
}
