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
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.monitoring.farmasidinkesminahasa.R
import com.monitoring.farmasidinkesminahasa.model.SensorResponse
import com.monitoring.farmasidinkesminahasa.service.ApiService
import com.monitoring.farmasidinkesminahasa.service.RetrofitClient
import com.monitoring.farmasidinkesminahasa.view.CircularProgressView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class NotificationFragment : Fragment() {

    private lateinit var suhuValue: TextView
    private lateinit var kelembapanValue: TextView
    private lateinit var listView: ListView
    private var allHistoryData: List<com.monitoring.farmasidinkesminahasa.model.HistoryItem>? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_notification, container, false)

        val suhuProgress: CircularProgressView = view.findViewById(R.id.suhuProgress)
        val kelembapanProgress: CircularProgressView = view.findViewById(R.id.kelembapanProgress)

        // Inisialisasi TextView
        listView = view.findViewById(R.id.listView)
        suhuValue = view.findViewById(R.id.suhuValue)
        kelembapanValue = view.findViewById(R.id.kelembapanValue)


        // Fetch data from API
        fetchSensorData(suhuProgress, kelembapanProgress)
        fetchDataFromApi()

        return view
    }

    private fun fetchDataFromApi() {
        val service = RetrofitClient.instance.getSensorData()

        service.enqueue(object : Callback<SensorResponse> {
            override fun onResponse(
                call: Call<SensorResponse>,
                response: Response<SensorResponse>
            ) {
                if (response.isSuccessful) {
                    val sensorResponse = response.body()
                    sensorResponse?.let {
                        allHistoryData = it.History
                        updateListView(it.History)
                    }
                } else {
                    Log.e("HistoryFragment", "Response failed: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<SensorResponse>, t: Throwable) {
                Log.e("HistoryFragment", "API call failed: ${t.message}")
            }
        })
    }

    private fun updateListView(history: List<com.monitoring.farmasidinkesminahasa.model.HistoryItem>) {
        val listData = history.map {
            "Timestamp: ${it.Timestamp}\n" +
                    "Kelembaban: ${it.Kelembaban}%\n" +
                    "Suhu: ${it.Suhu}°C\n" +
                    "Control: ${it.Control}"
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

    private fun fetchSensorData(
        suhuProgress: CircularProgressView,
        kelembapanProgress: CircularProgressView
    ) {

            val apiService = RetrofitClient.instance.getSensorData()

            apiService.enqueue(object : Callback<SensorResponse> {
                override fun onResponse(
                    call: Call<SensorResponse>,
                    response: Response<SensorResponse>
                ) {
                    if (response.isSuccessful) {
                        val sensorData = response.body()
                        Log.d("APIResponse", "Response body: $sensorData")

                        if (sensorData != null) {
                            // Update progress views
                            animateProgress(suhuProgress, sensorData.Suhu.toFloat())
                            animateProgress(kelembapanProgress, sensorData.Kelembaban.toFloat())

                            // Update TextView values
                            suhuValue.text = "${sensorData.Suhu}°C"
                            kelembapanValue.text = "${sensorData.Kelembaban}%"
                        } else {
                            view?.context?.let {
                                Toast.makeText(it, "Data kosong", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        view?.context?.let {
                            Toast.makeText(it, "Gagal mengambil data", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                override fun onFailure(call: Call<SensorResponse>, t: Throwable) {
                    view?.context?.let {
                        Toast.makeText(it, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                        Log.e("NetworkError", "Failed to fetch data: ${t.message}", t)
                    }
                }
            })
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
