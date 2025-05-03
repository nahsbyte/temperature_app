package com.monitoring.farmasidinkesminahasa.fragment

import android.animation.ValueAnimator
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.monitoring.farmasidinkesminahasa.R
import com.monitoring.farmasidinkesminahasa.model.HistoryItemResponse
import com.monitoring.farmasidinkesminahasa.service.RetrofitClient
import com.monitoring.farmasidinkesminahasa.view.CircularProgressView
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class NotificationFragment(private val payloadJson: String? = null) : Fragment() {
    private lateinit var suhuValue: TextView
    private lateinit var kelembapanValue: TextView
    private lateinit var notificationText: TextView
    private lateinit var alertBar: TextView
    private lateinit var suhuProgress: CircularProgressView
    private lateinit var kelembapanProgress: CircularProgressView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_notification, container, false)

        suhuValue = view.findViewById(R.id.suhuValue)
        kelembapanValue = view.findViewById(R.id.kelembapanValue)
        notificationText = view.findViewById(R.id.notificationText)
        alertBar = view.findViewById(R.id.alertBar)
        suhuProgress = view.findViewById(R.id.suhuProgress)
        kelembapanProgress = view.findViewById(R.id.kelembapanProgress)

        val jsonObject = payloadJson?.let { JSONObject(it) }
        val suhu = jsonObject?.optString("temperature") ?: "-"
        val kelembapan = jsonObject?.optString("humidity") ?: "-"
        val pesan = jsonObject?.optString("message") ?: "Tidak ada pesan"

        // Update progress views
        suhu.toFloatOrNull()?.let { animateProgress(suhuProgress, it) }
        kelembapan.toFloatOrNull()?.let { animateProgress(kelembapanProgress, it) }

        // Update text views with styled text
        suhuValue.text = "$suhu°C"
        kelembapanValue.text = "$kelembapan%"

        // Fuzzy Tsukamoto logic
        val suhuValueFloat = suhu.toFloatOrNull() ?: 0f
        val kelembapanValueFloat = kelembapan.toFloatOrNull() ?: 0f
        val tempFuzzy = fuzzyTemperature(suhuValueFloat)
        val humidityFuzzy = fuzzyHumidity(kelembapanValueFloat)
        val tempCategory = tempFuzzy.maxByOrNull { it.value }?.key ?: "normal"
        val humidityCategory = humidityFuzzy.maxByOrNull { it.value }?.key ?: "normal"

        // Rekomendasi perangkat
        val devicesToTurnOn = mutableListOf<String>()
        if (tempCategory == "cold") devicesToTurnOn.add("Pemanas Ruangan")
        else if (tempCategory == "hot") devicesToTurnOn.add("Kipas Pendingin")
        if (humidityCategory == "low") devicesToTurnOn.add("Uap Air")

        // Pesan fuzzy
        val fuzzyMessage = "Suhu: $tempCategory ($suhu°C), Kelembapan: $humidityCategory ($kelembapan%)"
        val deviceMessage = if (devicesToTurnOn.isNotEmpty())
            "Perangkat yang disarankan dinyalakan: ${devicesToTurnOn.joinToString(", ")}"
        else
            "Tidak ada perangkat yang perlu dinyalakan."

        // Format and display notification message
        val formattedMessage = "📌 $pesan\n\n" +
                "Suhu: $suhu°C\n" +
                "Kelembapan: $kelembapan%"
        notificationText.text = formattedMessage + "\n\n" + fuzzyMessage + "\n" + deviceMessage

        // ALERT BAR LOGIC BERBASIS FUZZY + ALAT
        if (tempCategory == "normal" && humidityCategory == "normal") {
            alertBar.visibility = View.VISIBLE
            alertBar.text = "Status normal: Suhu dan kelembapan berada pada rentang aman."
            alertBar.setBackgroundColor(Color.parseColor("#43A047")) // Hijau
        } else if (tempCategory == "hot") {
            alertBar.visibility = View.VISIBLE
            alertBar.text = "MENYALAKAN KIPAS ANGIN - SUHU TERLALU PANAS : $suhu°C"
            alertBar.setBackgroundColor(Color.parseColor("#E53935")) // Merah
        } else if (tempCategory == "cold") {
            alertBar.visibility = View.VISIBLE
            alertBar.text = "MENYALAKAN PEMANAS - SUHU TERLALU RENDAH : $suhu°C"
            alertBar.setBackgroundColor(Color.parseColor("#1E88E5")) // Biru
        } else if (humidityCategory == "low") {
            alertBar.visibility = View.VISIBLE
            alertBar.text = "MENYALAKAN HUMIDIFIER - KELEMBABAN TERLALU RENDAH : $kelembapan%"
            alertBar.setBackgroundColor(Color.parseColor("#1E88E5")) // Biru
        } else if (humidityCategory == "high") {
            alertBar.visibility = View.VISIBLE
            alertBar.text = "PERINGATAN: Kelembaban terlalu tinggi! ($kelembapan%)\nNyalakan dehumidifier atau AC jika tersedia."
            alertBar.setBackgroundColor(Color.parseColor("#FB8C00")) // Oranye
        } else {
            alertBar.visibility = View.GONE
        }

        return view
    }

    private fun animateProgress(progressView: CircularProgressView, targetValue: Float) {
        val animator = ValueAnimator.ofFloat(0f, targetValue)
        animator.duration = 2000 // Durasi animasi
        animator.addUpdateListener { animation ->
            progressView.setProgress(animation.animatedValue as Float)
        }
        animator.start()
    }

    private fun fuzzyTemperature(temp: Float): Map<String, Float> {
        val cold = if (temp <= 0) 1f else if (temp >= 22) 0f else (22 - temp) / 22f
        val normal = if (temp <= 22) 0f else if (temp >= 30) 0f else (temp - 22) / 8f
        val hot = if (temp <= 30) 0f else if (temp >= 40) 1f else (temp - 30) / 10f
        return mapOf("cold" to cold, "normal" to normal, "hot" to hot)
    }

    private fun fuzzyHumidity(humidity: Float): Map<String, Float> {
        val low = if (humidity <= 0) 1f else if (humidity >= 40) 0f else (40 - humidity) / 40f
        val normal = if (humidity <= 40) 0f else if (humidity >= 60) 0f else (humidity - 40) / 20f
        val high = if (humidity <= 60) 0f else if (humidity >= 100) 1f else (humidity - 60) / 40f
        return mapOf("low" to low, "normal" to normal, "high" to high)
    }
}
