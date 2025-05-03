package com.monitoring.farmasidinkesminahasa

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.messaging.FirebaseMessaging
import com.monitoring.farmasidinkesminahasa.helper.SharedPreferencesHelper

class MainActivity : AppCompatActivity() {
    private val sharedPreferencesHelper by lazy {
        SharedPreferencesHelper(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()

        splashScreen.setOnExitAnimationListener { splashScreenViewProvider ->
            splashScreenViewProvider.view.animate().alpha(0f).withEndAction {
                splashScreenViewProvider.remove()
            }
        }

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        FirebaseMessaging.getInstance().subscribeToTopic("farmasi")
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("FCM", "Subscribed to topic farmasi")
                }
            }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 101)
        }

        val fromFcm = intent.getBooleanExtra("from_fcm", false)
        val body = intent.getStringExtra("message_body")

        if (fromFcm) {
            Log.d("HomeActivity", "Opened from FCM: $body")
            // You can navigate to specific fragment or show dialog
            val intent = Intent(this, HomeActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            intent.putExtra("navigate_to", "notification")
            intent.putExtra("from_fcm", true)
            intent.putExtra("message_body", body)

            startActivity(intent)
        } else {
            Handler(Looper.getMainLooper()).postDelayed({
                checkRedirection()
            }, 2000) // 2 seconds delay
        }

    }

    override fun onStart() {
        super.onStart()
    }

    private fun checkRedirection() {
        val isLoggedIn = sharedPreferencesHelper.getBoolean("is_logged_in", false)

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.POST_NOTIFICATIONS
                ),
                100
            )
        }

        if (!isLoggedIn) {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }
    }
}