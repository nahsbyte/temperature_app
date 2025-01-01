package com.monitoring.farmasidinkesminahasa.fragment

import android.animation.ValueAnimator
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.monitoring.farmasidinkesminahasa.R
import com.monitoring.farmasidinkesminahasa.model.SensorResponse
import com.monitoring.farmasidinkesminahasa.service.SensorApiService
import com.monitoring.farmasidinkesminahasa.view.CircularProgressView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class HomeFragment : Fragment() {

    // Deklarasi TextView di level kelas
    private lateinit var suhuValue: TextView
    private lateinit var kelembapanValue: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        // Suhu & Kelembapan Progress Views
        val suhuProgress: CircularProgressView = view.findViewById(R.id.suhuProgress)
        val kelembapanProgress: CircularProgressView = view.findViewById(R.id.kelembapanProgress)

        // Inisialisasi TextView
        suhuValue = view.findViewById(R.id.suhuValue)
        kelembapanValue = view.findViewById(R.id.kelembapanValue)

        // Fetch data from API
        fetchSensorData(suhuProgress, kelembapanProgress)

        return view
    }

    private fun fetchSensorData(suhuProgress: CircularProgressView, kelembapanProgress: CircularProgressView) {
        val retrofit = Retrofit.Builder()
            .baseUrl("http://172.30.24.218:3000/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(SensorApiService::class.java)
        apiService.getSensorData().enqueue(object : Callback<SensorResponse> {
            override fun onResponse(call: Call<SensorResponse>, response: Response<SensorResponse>) {
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
