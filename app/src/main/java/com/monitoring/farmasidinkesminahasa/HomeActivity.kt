package com.monitoring.farmasidinkesminahasa

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowInsetsController
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.messaging.FirebaseMessaging
import com.monitoring.farmasidinkesminahasa.fragment.HistoryFragment
import com.monitoring.farmasidinkesminahasa.fragment.HomeFragment
import com.monitoring.farmasidinkesminahasa.fragment.NotificationFragment
import com.monitoring.farmasidinkesminahasa.fragment.ToolsFragment

class HomeActivity : AppCompatActivity() {
    private var body: String? = null
    private var fromFcm: Boolean = false
    lateinit var context: Context
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        setupFullscreenLayout()
        applyInsetsToMainView()
        context = this
        replaceFragment(HomeFragment())
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_nav)
        val toolbar = findViewById<MaterialToolbar>(R.id.topAppBar)

        ViewCompat.setOnApplyWindowInsetsListener(toolbar) { view, insets ->
            val topInset = insets.getInsets(WindowInsetsCompat.Type.systemBars()).top
            view.setPadding(0, topInset, 0, 0)
            insets
        }

        toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.action_notifications -> {
                    Log.d("Navigation", "Notification clicked")
                    replaceFragment(NotificationFragment(null)) // Navigate to Notification Fragment
                    true
                }

                else -> false
            }
        }

        if (savedInstanceState == null) {
            replaceFragment(HomeFragment())
            bottomNavigationView.selectedItemId = R.id.nav_home
        }

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    Log.d("Navigation", "Home clicked")
                    replaceFragment(HomeFragment())
                    true
                }

                R.id.nav_history -> {
                    Log.d("Navigation", "History clicked")
                    replaceFragment(HistoryFragment())
                    true
                }

                R.id.nav_tools -> {
                    Log.d("Navigation", "Tools clicked")
                    replaceFragment(ToolsFragment())
                    true
                }

                else -> false
            }
        }

        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("FCM", "Fetching FCM registration token failed", task.exception)
                return@addOnCompleteListener
            }

            // Dapatkan token FCM
            val token = task.result
            Log.d("FCM", "Device FCM Token: $token")

            // Kirim ke server kamu kalau perlu
        }
        handleFcmIntentIfAvailable(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        intent.let { handleFcmIntentIfAvailable(it) }
    }

    override fun onResume() {
        super.onResume()
    }

    private fun replaceFragment(fragment: Fragment) {

        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragment_container, fragment)
        fragmentTransaction.commit()
    }

    private fun setupFullscreenLayout() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
            window.insetsController?.apply {
                setSystemBarsAppearance(
                    0, WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                )
            }
            window.statusBarColor = Color.TRANSPARENT // Pastikan ini transparan
        } else {
            @Suppress("DEPRECATION") window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            @Suppress("DEPRECATION") window.statusBarColor =
                Color.TRANSPARENT // Pastikan ini transparan
        }
    }


    private fun applyInsetsToMainView() {
        val mainView = findViewById<View>(R.id.main)
//        val bellIcon = findViewById<View>(R.id.bell_icon)

        ViewCompat.setOnApplyWindowInsetsListener(mainView) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            // Terapkan padding untuk main layout
            mainView.setPadding(
                systemBars.left,
                0, // Abaikan padding di atas (sudah dikelola secara manual di bell_icon)
                systemBars.right,
                systemBars.bottom
            )

            // Geser bell_icon agar tidak terhalang status bar
//            bellIcon.translationY = systemBars.top.toFloat()
            insets
        }
    }

    private fun handleFcmIntentIfAvailable(intent: Intent) {
        val fromFcm = intent.getBooleanExtra("from_fcm", false)
        val body = intent.getStringExtra("message_body")

        if (fromFcm && body != null) {
            replaceFragment(NotificationFragment(body))
        }
    }

}
