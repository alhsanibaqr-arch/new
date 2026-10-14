package com.vardely.app

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.widget.Button
import android.text.InputFilter
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class AppLockActivity : AppCompatActivity() {
    private lateinit var store: BlocklistStore
    private var targetPackage: String? = null
    private lateinit var pinInput: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        store = BlocklistStore(this)
        targetPackage = intent.getStringExtra(EXTRA_PACKAGE)
        if (targetPackage == null || !store.hasPin()) { goHome(); return }

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            val density = resources.displayMetrics.density
            setPadding((32 * density).toInt(), (36 * density).toInt(), (32 * density).toInt(), (36 * density).toInt())
            setBackgroundColor(0xFFF4F8F6.toInt())
        }
        root.addView(TextView(this).apply {
            text = getString(R.string.app_lock_title)
            textSize = 25f
            setTextColor(0xFF102A2A.toInt())
            setTypeface(null, android.graphics.Typeface.BOLD)
            gravity = Gravity.CENTER
        })
        root.addView(TextView(this).apply {
            text = getString(R.string.app_lock_subtitle)
            textSize = 15f
            setTextColor(0xFF617574.toInt())
            gravity = Gravity.CENTER
            setPadding(0, 10, 0, 24)
        })
        pinInput = EditText(this).apply {
            hint = getString(R.string.enter_pin)
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD
            setSingleLine(true)
            filters = arrayOf(InputFilter.LengthFilter(12))
            gravity = Gravity.CENTER
        }
        root.addView(pinInput, LinearLayout.LayoutParams(-1, 56))
        root.addView(Button(this).apply {
            text = getString(R.string.unlock)
            setOnClickListener { unlock() }
        }, LinearLayout.LayoutParams(-1, (54 * resources.displayMetrics.density).toInt()).apply { topMargin = (16 * resources.displayMetrics.density).toInt() })
        root.addView(Button(this).apply {
            text = getString(R.string.back_home)
            setOnClickListener { goHome() }
        }, LinearLayout.LayoutParams(-1, (50 * resources.displayMetrics.density).toInt()).apply { topMargin = (8 * resources.displayMetrics.density).toInt() })
        setContentView(root)
    }

    private fun unlock() {
        val pin = pinInput.text.toString()
        if (!store.verifyPin(pin)) {
            pinInput.text?.clear()
            Toast.makeText(this, R.string.wrong_pin, Toast.LENGTH_SHORT).show()
            return
        }
        val target = targetPackage ?: return goHome()
        store.allowPackageTemporarily(target)
        packageManager.getLaunchIntentForPackage(target)?.let {
            it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(it)
        }
        finish()
    }

    private fun goHome() {
        startActivity(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        finish()
    }

    companion object { const val EXTRA_PACKAGE = "target_package" }
}
