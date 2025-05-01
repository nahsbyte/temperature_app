package com.monitoring.farmasidinkesminahasa.fragment

import android.animation.ValueAnimator
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.StateListDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
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
    private lateinit var listView: ListView
    private var allHistoryData: List<com.monitoring.farmasidinkesminahasa.model.HistoryItemResponse>? =
        null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_notification, container, false)

        suhuValue = view.findViewById(R.id.suhuValue)
        kelembapanValue = view.findViewById(R.id.kelembapanValue)
        listView = view.findViewById(R.id.listView)

        val jsonObject = payloadJson?.let { JSONObject(it) }
        val suhu = jsonObject?.optString("temperature") ?: "-"
        val kelembapan = jsonObject?.optString("humidity") ?: "-"
        val pesan = jsonObject?.optString("message") ?: "Tidak ada pesan"

        // Tampilkan suhu dan kelembaban ke TextView
        suhuValue.text = "$suhu°C"
        kelembapanValue.text = "$kelembapan%"

        // Tambahkan ke ListView jika perlu
        val list = listOf("📌 Notifikasi: $pesan\nSuhu: $suhu°C\nKelembapan: $kelembapan%")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, list)
        listView.adapter = adapter

        return view
    }

    private fun fetchDataFromApi() {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val calendar = Calendar.getInstance()

        val endDate = sdf.format(calendar.time)
        calendar.add(Calendar.DAY_OF_MONTH, -1)
        val startDate = sdf.format(calendar.time)

        val service = RetrofitClient.instance.getHistorySensorData(startDate, endDate)

        service.enqueue(object : Callback<List<HistoryItemResponse>> {
            override fun onResponse(
                call: Call<List<HistoryItemResponse>>,
                response: Response<List<HistoryItemResponse>>
            ) {
                if (response.isSuccessful) {
                    val sensorResponse = response.body()
                    sensorResponse?.let {
                        allHistoryData = it
                        updateListView(it)
                    }
                } else {
                    Log.e("HistoryFragment", "Response failed: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<List<HistoryItemResponse>>, t: Throwable) {
                Log.e("HistoryFragment", "API call failed: ${t.message}")
            }
        })
    }

    private fun updateListView(history: List<com.monitoring.farmasidinkesminahasa.model.HistoryItemResponse>) {
        val listData = history.map {
            "Timestamp: ${it.timestamp}\n" +
                    "Kelembaban: ${it.humidity}%\n" +
                    "Suhu: ${it.temperature}°C\n"
        }

        val adapter = object :
            ArrayAdapter<String>(requireContext(), android.R.layout.simple_list_item_1, listData) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent)
                val textView = view.findViewById<TextView>(android.R.id.text1)
                textView.setTextColor(Color.BLACK)
                textView.textSize = 16f
                textView.setPadding(16, 16, 16, 16)

                val drawable = StateListDrawable()
                drawable.addState(
                    intArrayOf(android.R.attr.state_pressed),
                    ColorDrawable(Color.LTGRAY)
                )
                drawable.addState(intArrayOf(), ColorDrawable(Color.WHITE))
                view.background = drawable

                return view
            }
        }

        listView.adapter = adapter

    }

    private fun animateProgress(progressView: CircularProgressView, targetValue: Float) {
        val animator = ValueAnimator.ofFloat(0f, targetValue)
        animator.duration = 2000 // Durasi animasi
        animator.addUpdateListener { animation ->
            progressView.setProgress(animation.animatedValue as Float)
        }
        animator.start()
    }

}
