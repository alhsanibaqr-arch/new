package com.vardely.app

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.getSystemService
import androidx.core.os.LocaleListCompat
import com.google.android.material.card.MaterialCardView
import com.google.android.material.switchmaterial.SwitchMaterial
import java.time.LocalDate

class MainActivity : AppCompatActivity() {
    private lateinit var store: BlocklistStore
    private lateinit var content: FrameLayout
    private lateinit var appsContainer: LinearLayout
    private var selectedTab = "home"
    private val ink = 0xFF102A2A.toInt()
    private val muted = 0xFF617574.toInt()
    private val accent = 0xFF087F73.toInt()
    private val canvas = 0xFFF4F8F6.toInt()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        store = BlocklistStore(this)
        window.statusBarColor = canvas
        window.navigationBarColor = canvas
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        setContentView(buildShell())
        renderTab("home")
    }

    override fun onResume() {
        super.onResume()
        if (::store.isInitialized && ::content.isInitialized) renderTab(selectedTab)
    }

    private fun buildShell(): View {
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setBackgroundColor(canvas); layoutDirection = View.LAYOUT_DIRECTION_LOCALE }
        val header = LinearLayout(this).apply { gravity = Gravity.CENTER_VERTICAL; setPadding(dp(22), dp(18), dp(22), dp(14)) }
        val mark = TextView(this).apply { text = "✿"; textSize = 23f; gravity = Gravity.CENTER; setTextColor(-1); background = rounded(accent, 16); layoutParams = LinearLayout.LayoutParams(dp(44), dp(44)) }
        header.addView(mark)
        header.addView(text(getString(R.string.app_name), 22f, ink, true).apply { setPadding(dp(12), 0, 0, 0) })
        content = FrameLayout(this)
        root.addView(header)
        root.addView(content, LinearLayout.LayoutParams(-1, 0, 1f))
        val nav = LinearLayout(this).apply { gravity = Gravity.CENTER; setPadding(dp(8), dp(7), dp(8), dp(10)); setBackgroundColor(-1) }
        listOf(Triple("home", "⌂", R.string.nav_home), Triple("apps", "▦", R.string.nav_apps), Triple("stats", "▤", R.string.nav_stats), Triple("settings", "⚙", R.string.nav_settings)).forEach { (tab, icon, labelId) ->
            val item = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; gravity = Gravity.CENTER; setPadding(dp(5), dp(4), dp(5), dp(4)); isClickable = true; isFocusable = true; setOnClickListener { renderTab(tab) } }
            item.addView(text(icon, 21f, accent).apply { gravity = Gravity.CENTER })
            item.addView(text(getString(labelId), 11f, muted).apply { gravity = Gravity.CENTER })
            nav.addView(item, LinearLayout.LayoutParams(0, -2, 1f))
        }
        root.addView(nav)
        return root
    }

    private fun renderTab(tab: String) {
        selectedTab = tab
        content.removeAllViews()
        val page = when (tab) { "apps" -> buildAppsPage(); "stats" -> buildStatsPage(); "settings" -> buildSettingsPage(); else -> buildHomePage() }
        content.addView(page, FrameLayout.LayoutParams(-1, -1))
    }

    private fun buildHomePage(): View {
        val column = pageColumn()
        column.addView(title(getString(R.string.home_title), getString(R.string.home_subtitle)))
        val status = card()
        val body = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(17), dp(16), dp(17), dp(16)) }
        val line = LinearLayout(this).apply { gravity = Gravity.CENTER_VERTICAL }
        line.addView(text(if (store.isBlockingEnabled()) "🛡️" else "🔒", 27f, accent).apply { gravity = Gravity.CENTER; background = rounded(0xFFE4F4EF.toInt(), 16); layoutParams = LinearLayout.LayoutParams(dp(50), dp(50)) })
        val state = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(12), 0, 0, 0) }
        state.addView(text(getString(if (store.isBlockingEnabled()) R.string.protection_on else R.string.protection_off), 16f, ink, true))
        state.addView(text(getString(if (isAccessibilityEnabled()) R.string.service_ready else R.string.service_needs_setup), 12f, muted))
        line.addView(state, LinearLayout.LayoutParams(0, -2, 1f))
        val toggle = SwitchMaterial(this).apply {
            isChecked = store.isBlockingEnabled()
            setOnCheckedChangeListener { _, enabled ->
                if (enabled && !isAccessibilityEnabled()) { isChecked = false; showAccessibilityDisclosure() }
                else { store.setBlockingEnabled(enabled); renderTab("home") }
            }
        }
        line.addView(toggle)
        body.addView(line)
        body.addView(View(this).apply { setBackgroundColor(0xFFE7EEEB.toInt()) }, LinearLayout.LayoutParams(-1, dp(1)).apply { topMargin = dp(13); bottomMargin = dp(10) })
        body.addView(text(getString(R.string.blocked_apps_count, store.blockedPackages().size), 14f, muted))
        if (!isAccessibilityEnabled()) body.addView(button(getString(R.string.enable_protection), true) { showAccessibilityDisclosure() }, LinearLayout.LayoutParams(-1, dp(48)).apply { topMargin = dp(13) })
        status.addView(body)
        column.addView(status)
        column.addView(section(getString(R.string.quick_actions)))
        listOf(Triple("▦", R.string.nav_apps, "apps"), Triple("◷", R.string.nav_stats, "stats"), Triple("⚙", R.string.nav_settings, "settings")).forEach { (icon, label, tab) ->
            val c = card()
            val row = LinearLayout(this).apply { gravity = Gravity.CENTER_VERTICAL; setPadding(dp(15), dp(11), dp(15), dp(11)); isClickable = true; setOnClickListener { renderTab(tab) } }
            row.addView(text(icon, 23f, accent, true))
            row.addView(text(getString(label), 15f, ink, true), LinearLayout.LayoutParams(0, -2, 1f).apply { marginStart = dp(13) })
            row.addView(text("‹", 23f, muted))
            c.addView(row)
            column.addView(c)
        }
        column.addView(section(getString(R.string.today_summary)))
        column.addView(card().apply { addView(text(getString(R.string.blocked_today, store.attemptsForDate(LocalDate.now().toString())), 15f, ink, true).apply { setPadding(dp(16), dp(17), dp(16), dp(17)) }) })
        return ScrollView(this).apply { clipToPadding = false; setPadding(dp(18), 0, dp(18), dp(12)); addView(column) }
    }

    private fun buildAppsPage(): View {
        val column = pageColumn()
        column.addView(title(getString(R.string.apps_title), getString(R.string.apps_subtitle)))
        val search = EditText(this).apply { hint = getString(R.string.search_apps); textSize = 15f; singleLine = true; setPadding(dp(14), 0, dp(14), 0); background = rounded(-1, 14) }
        column.addView(search, LinearLayout.LayoutParams(-1, dp(50)).apply { bottomMargin = dp(10) })
        appsContainer = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        column.addView(appsContainer)
        fillApps("")
        search.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { fillApps(s?.toString().orEmpty()) }
            override fun afterTextChanged(s: Editable?) = Unit
        })
        return ScrollView(this).apply { clipToPadding = false; setPadding(dp(18), 0, dp(18), dp(14)); addView(column) }
    }

    private fun fillApps(query: String) {
        if (!::appsContainer.isInitialized) return
        appsContainer.removeAllViews()
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val results = if (Build.VERSION.SDK_INT >= 33) packageManager.queryIntentActivities(intent, PackageManager.ResolveInfoFlags.of(0)) else {
            @Suppress("DEPRECATION") packageManager.queryIntentActivities(intent, 0)
        }
        val apps = results.asSequence().mapNotNull { resolve -> resolve.activityInfo?.applicationInfo?.let { Triple(it, resolve.loadLabel(packageManager).toString(), resolve.loadIcon(packageManager)) } }
            .filter { it.first.packageName != packageName && it.second.contains(query, true) }
            .distinctBy { it.first.packageName }.sortedBy { it.second.lowercase() }.toList()
        if (apps.isEmpty()) { appsContainer.addView(text(getString(R.string.no_apps), 14f, muted).apply { setPadding(dp(8), dp(18), dp(8), dp(18)) }); return }
        apps.forEach { (info, label, icon) ->
            val c = card()
            val row = LinearLayout(this).apply { gravity = Gravity.CENTER_VERTICAL; setPadding(dp(11), dp(7), dp(11), dp(7)) }
            row.addView(ImageView(this).apply { setImageDrawable(icon); contentDescription = label }, LinearLayout.LayoutParams(dp(42), dp(42)))
            row.addView(text(label, 14f, ink, true).apply { maxLines = 2; setPadding(dp(11), 0, dp(6), 0) }, LinearLayout.LayoutParams(0, -2, 1f))
            row.addView(SwitchMaterial(this).apply {
                isChecked = store.blockedPackages().contains(info.packageName)
                contentDescription = getString(R.string.block_app_accessibility, label)
                setOnCheckedChangeListener { _, checked -> store.setAppBlocked(info.packageName, checked) }
            })
            c.addView(row)
            appsContainer.addView(c)
        }
    }

    private fun buildStatsPage(): View {
        val column = pageColumn()
        column.addView(title(getString(R.string.stats_title), getString(R.string.stats_subtitle)))
        val today = store.attemptsForDate(LocalDate.now().toString())
        val week = (0..6).sumOf { store.attemptsForDate(LocalDate.now().minusDays(it.toLong()).toString()) }
        column.addView(statCard("◷", getString(R.string.blocked_today_label), today.toString()))
        column.addView(statCard("▥", getString(R.string.blocked_week_label), week.toString()))
        column.addView(statCard("▦", getString(R.string.apps_protected), store.blockedPackages().size.toString()))
        column.addView(text(getString(R.string.stats_privacy_note), 13f, muted).apply { setPadding(dp(4), dp(8), dp(4), 0) })
        return ScrollView(this).apply { setPadding(dp(18), 0, dp(18), dp(18)); addView(column) }
    }

    private fun buildSettingsPage(): View {
        val column = pageColumn()
        column.addView(title(getString(R.string.settings_title), getString(R.string.settings_subtitle)))
        val language = card()
        val row = LinearLayout(this).apply { gravity = Gravity.CENTER_VERTICAL; setPadding(dp(15), dp(12), dp(12), dp(12)) }
        row.addView(text("🌐", 22f, accent))
        row.addView(text(getString(R.string.language), 15f, ink, true), LinearLayout.LayoutParams(0, -2, 1f).apply { marginStart = dp(12) })
        row.addView(button(getString(R.string.language_switch), false) {
            val current = AppCompatDelegate.getApplicationLocales().toLanguageTags()
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(if (current.startsWith("ar")) "en" else "ar"))
        })
        language.addView(row)
        column.addView(language)
        val access = card()
        val accessBody = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(15), dp(13), dp(15), dp(13)) }
        accessBody.addView(text(getString(R.string.accessibility_title), 16f, ink, true))
        accessBody.addView(text(getString(R.string.accessibility_explanation), 13f, muted).apply { setPadding(0, dp(6), 0, dp(10)) })
        accessBody.addView(button(getString(if (isAccessibilityEnabled()) R.string.open_accessibility_settings else R.string.enable_protection), true) {
            if (isAccessibilityEnabled()) startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) else showAccessibilityDisclosure()
        })
        access.addView(accessBody)
        column.addView(access)
        column.addView(card().apply { addView(text(getString(R.string.about_text), 13f, muted).apply { setPadding(dp(15), dp(13), dp(15), dp(13)) }) })
        return ScrollView(this).apply { setPadding(dp(18), 0, dp(18), dp(18)); addView(column) }
    }

    private fun showAccessibilityDisclosure() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle(R.string.accessibility_disclosure_title)
            .setMessage(R.string.accessibility_disclosure_body)
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.continue_label) { _, _ ->
                store.setDisclosureAccepted(true)
                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            }.show()
    }

    private fun isAccessibilityEnabled(): Boolean =
        getSystemService<android.view.accessibility.AccessibilityManager>()?.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
            ?.any { it.resolveInfo.serviceInfo.packageName == packageName && it.resolveInfo.serviceInfo.name == VardelyAccessibilityService::class.java.name } == true

    private fun statCard(icon: String, label: String, value: String): View {
        val c = card()
        val row = LinearLayout(this).apply { gravity = Gravity.CENTER_VERTICAL; setPadding(dp(17), dp(15), dp(17), dp(15)) }
        row.addView(text(icon, 24f, accent, true))
        row.addView(text(label, 14f, muted, true), LinearLayout.LayoutParams(0, -2, 1f).apply { marginStart = dp(13) })
        row.addView(text(value, 25f, ink, true))
        c.addView(row)
        return c
    }

    private fun title(main: String, sub: String) = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL; setPadding(dp(2), dp(8), dp(2), dp(14))
        addView(text(main, 24f, ink, true))
        addView(text(sub, 13f, muted).apply { setPadding(0, dp(4), 0, 0) })
    }
    private fun section(value: String) = text(value, 16f, ink, true).apply { setPadding(dp(2), dp(17), dp(2), dp(8)) }
    private fun pageColumn() = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
    private fun card() = MaterialCardView(this).apply {
        radius = dp(17).toFloat(); cardElevation = dp(1).toFloat(); setCardBackgroundColor(-1); strokeWidth = dp(1); strokeColor = 0xFFE8EFEC.toInt()
        layoutParams = LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(9) }
    }
    private fun text(value: String, size: Float, color: Int, bold: Boolean = false) = TextView(this).apply {
        text = value; textSize = size; setTextColor(color); if (bold) setTypeface(null, android.graphics.Typeface.BOLD)
    }
    private fun button(label: String, filled: Boolean, action: () -> Unit): View =
        com.google.android.material.button.MaterialButton(this).apply {
            text = label; isAllCaps = false; setOnClickListener { action() }
            if (filled) { setBackgroundColor(accent); setTextColor(-1) }
        }
    private fun rounded(color: Int, radius: Int) = android.graphics.drawable.GradientDrawable().apply { setColor(color); cornerRadius = dp(radius).toFloat() }
    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()
}
