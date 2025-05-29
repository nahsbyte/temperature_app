package com.monitoring.farmasidinkesminahasa.fragment

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import android.widget.Switch
import androidx.compose.ui.input.key.type
import com.google.firebase.messaging.FirebaseMessaging
import com.monitoring.farmasidinkesminahasa.R
import com.monitoring.farmasidinkesminahasa.service.RetrofitClient
import com.monitoring.farmasidinkesminahasa.service.RetrofitClientNodeMcu
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.text.removeSurrounding

class SettingFragment : Fragment() {

    private lateinit var editTextSsid: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var buttonSave: Button
    private lateinit var switchFcmAlerts: Switch

    private val PREFS_NAME = "AppSettings"
    private val PREF_FCM_ALERTS_ENABLED = "fcm_alerts_enabled"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_setting, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        editTextSsid = view.findViewById(R.id.editTextSsid)
        editTextPassword = view.findViewById(R.id.editTextPassword)
        buttonSave = view.findViewById(R.id.buttonSave)
        switchFcmAlerts = view.findViewById(R.id.switchFcmAlerts)

        // Load saved state for FCM alerts
        loadFcmAlertsState()

        buttonSave.setOnClickListener {
            val ssid = editTextSsid.text.toString()
            val password = editTextPassword.text.toString()

            if (ssid.isNotEmpty() && password.isNotEmpty()) {
                if (isConnectedToWifi("NodeMCU_AP")) {
                    val call = RetrofitClientNodeMcu.instance.updateWifiLocal(ssid, password)
                    call.enqueue(object : Callback<Void> {
                        override fun onResponse(call: Call<Void>, response: Response<Void>) {
                            if (response.isSuccessful) {
                                Toast.makeText(
                                    context,
                                    "Konfigurasi WiFi berhasil disimpan ke lokal.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                Toast.makeText(
                                    context,
                                    "Gagal menyimpan konfigurasi ke lokal: ${response.code()}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }

                        override fun onFailure(call: Call<Void>, t: Throwable) {
                            Toast.makeText(
                                context,
                                "Kesalahan jaringan: ${t.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    })
                } else {
                    Toast.makeText(
                        context,
                        "Harap terhubung ke WiFi $ssid sebelum menyimpan konfigurasi.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                Toast.makeText(context, "SSID dan Password tidak boleh kosong", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        // Optional: Save FCM state immediately when the switch is toggled
        switchFcmAlerts.setOnCheckedChangeListener { _, isChecked ->
            saveFcmAlertsState(isChecked)
            val topic = "farmasi" // You can change this topic name as needed
            if (isChecked) {
                FirebaseMessaging.getInstance().subscribeToTopic(topic)
                    .addOnCompleteListener { task ->
                        var msg = "Subscribed to FCM Alerts ($topic)"
                        if (!task.isSuccessful) {
                            msg = "Subscription to FCM Alerts ($topic) failed"
                        }
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    }
            } else {
                FirebaseMessaging.getInstance().unsubscribeFromTopic(topic)
                    .addOnCompleteListener { task ->
                        var msg = "Unsubscribed from FCM Alerts ($topic)"
                        if (!task.isSuccessful) {
                            msg = "Unsubscription from FCM Alerts ($topic) failed"
                        }
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }

    private fun saveFcmAlertsState(isEnabled: Boolean) {
        val sharedPrefs = activity?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        with(sharedPrefs?.edit()) {
            this?.putBoolean(PREF_FCM_ALERTS_ENABLED, isEnabled)
            this?.apply()
        }
    }

    private fun loadFcmAlertsState() {
        val sharedPrefs = activity?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val isEnabled =
            sharedPrefs?.getBoolean(PREF_FCM_ALERTS_ENABLED, true) ?: true // Default to enabled
        switchFcmAlerts.isChecked = isEnabled
    }

    // Method to check if connected to a specific Wi-Fi SSID
    private fun isConnectedToWifi(expectedSsid: String): Boolean {
        val connectivityManager =
            context?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
                ?: return false // Return false if context or service is null

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val capabilities =
                connectivityManager.getNetworkCapabilities(network) ?: return false

            if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                val wifiManager =
                    context?.applicationContext?.getSystemService(Context.WIFI_SERVICE) as WifiManager?
                        ?: return false // Return false if context or service is null
                val connectionInfo = wifiManager.connectionInfo
                // SSID might be enclosed in double quotes, remove them for comparison
                val currentSsid = connectionInfo.ssid.removeSurrounding("\"")
                return currentSsid == expectedSsid
            }
        } else {
            // For older versions (below Android M), use deprecated methods
            @Suppress("DEPRECATION")
            val networkInfo = connectivityManager.activeNetworkInfo ?: return false
            @Suppress("DEPRECATION")
            if (networkInfo.isConnected && networkInfo.type == ConnectivityManager.TYPE_WIFI) {
                val wifiManager =
                    context?.applicationContext?.getSystemService(Context.WIFI_SERVICE) as WifiManager?
                        ?: return false // Return false if context or service is null

                @Suppress("DEPRECATION")
                val connectionInfo = wifiManager.connectionInfo
                // SSID might be enclosed in double quotes, remove them for comparison
                @Suppress("DEPRECATION")
                val currentSsid = connectionInfo.ssid.removeSurrounding("\"")
                return currentSsid == expectedSsid
            }
        }
        return false
    }

}