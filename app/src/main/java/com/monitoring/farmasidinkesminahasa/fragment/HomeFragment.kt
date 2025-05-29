package com.monitoring.farmasidinkesminahasa.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.google.android.material.switchmaterial.SwitchMaterial
import com.monitoring.farmasidinkesminahasa.R
import com.monitoring.farmasidinkesminahasa.model.ConfigRequest
import com.monitoring.farmasidinkesminahasa.model.HistoryItemResponse
import com.monitoring.farmasidinkesminahasa.service.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeFragment : Fragment() {

    // Deklarasi TextView di level kelas
    private lateinit var suhuValue: TextView
    private lateinit var kelembapanValue: TextView
    private lateinit var cvSPlus: CardView
    private lateinit var cvSReset: CardView
    private lateinit var cvSMin: CardView
    private lateinit var cvKPlus: CardView
    private lateinit var cvKReset: CardView
    private lateinit var cvKMin: CardView
    private lateinit var btnSettings: ImageButton
    private lateinit var switchKipas: SwitchMaterial
    private lateinit var switchPemanas: SwitchMaterial
    private lateinit var switchUap: SwitchMaterial

    private var currentSuhu: Float = 0f
    private var realSuhu: Float = 0f
    private var customSuhu: Float = 0f

    private var currentKelembaban: Float = 0f
    private var realKelembaban: Float = 0f
    private var customKelembaban: Float = 0f

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        // Inisialisasi TextView
        suhuValue = view.findViewById(R.id.suhuValue)
        kelembapanValue = view.findViewById(R.id.kelembapanValue)
        cvSPlus = view.findViewById(R.id.cvSPlus)
        cvSReset = view.findViewById(R.id.cvSReset)
        cvSMin = view.findViewById(R.id.cvSMin)
        cvKPlus = view.findViewById(R.id.cvKPlus)
        cvKReset = view.findViewById(R.id.cvKReset)
        cvKMin = view.findViewById(R.id.cvKMin)
        btnSettings = view.findViewById(R.id.btnSettings)

        // Fetch data from API
        fetchSensorData()

        cvSPlus.setOnClickListener {
            addValue(1.0f, true)
            updateConfig(true, currentSuhu, false)
        }

        cvSMin.setOnClickListener {
            minValue(1.0f, true)
            updateConfig(true, currentSuhu, false)
        }

        cvKPlus.setOnClickListener {
            addValue(1.0f, false)
            updateConfig(false, currentKelembaban, false)
        }

        cvKMin.setOnClickListener {
            minValue(1.0f, false)
            updateConfig(false, currentKelembaban, false)
        }

        cvSReset.setOnClickListener {
            resetValue(true)
            updateConfig(true, 0f, true)
            fetchSensorData()
        }

        cvKReset.setOnClickListener {
            resetValue(false)
            updateConfig(false, 0f, true)
            fetchSensorData()
        }

        btnSettings.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, SettingFragment())
                .addToBackStack(null)
                .commit()
        }

        switchKipas = view.findViewById(R.id.switchKipas)
        switchPemanas = view.findViewById(R.id.switchPemanas)
        switchUap = view.findViewById(R.id.switchUap)

        switchKipas.setOnCheckedChangeListener { _, isChecked ->
            updateManualDeviceConfig("fan", isChecked)
        }
        switchPemanas.setOnCheckedChangeListener { _, isChecked ->
            updateManualDeviceConfig("heater", isChecked)
        }
        switchUap.setOnCheckedChangeListener { _, isChecked ->
            updateManualDeviceConfig("humidifier", isChecked)
        }

        return view
    }

    private fun resetValue(isSuhu: Boolean = true) {
        if (isSuhu) {
            currentSuhu = realSuhu
            suhuValue.text = "${currentSuhu}°C"
        } else {
            currentKelembaban = realKelembaban
            kelembapanValue.text = "${currentKelembaban}°C"
        }
    }

    private fun addValue(increment: Float, isSuhu: Boolean = true) {
        if (isSuhu) {
            currentSuhu += increment
            suhuValue.text = "${currentSuhu}°C"
        } else {
            currentKelembaban += increment
            kelembapanValue.text = "${currentKelembaban}°C"
        }
    }

    private fun minValue(increment: Float, isSuhu: Boolean = true) {
        if (isSuhu) {
            currentSuhu -= increment
            suhuValue.text = "${currentSuhu}°C"
        } else {
            currentKelembaban -= increment
            kelembapanValue.text = "${currentKelembaban}°C"
        }
    }

    private fun updateManualDeviceConfig(device: String, isOn: Boolean) {
        val configRequest = ConfigRequest(
            period = null,
            ssid = null,
            password = null,
            is_periodic_sensor = null,
            manual_fan = if (device == "fan") isOn else null,
            manual_heater = if (device == "heater") isOn else null,
            manual_humidifier = if (device == "humidifier") isOn else null,
            manual_humidity = null,
            manual_temp = null
        )

        RetrofitClient.instance.postConfig(configRequest).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                // Optionally handle success
                if (response.isSuccessful) {
                    view?.context?.let {
                        Toast.makeText(it, "Pengaturan $device berhasil diperbarui", Toast.LENGTH_SHORT)
                            .show()
                    }
                } else {
                    view?.context?.let {
                        Toast.makeText(it, "Gagal memperbarui pengaturan $device", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
            override fun onFailure(call: Call<Void>, t: Throwable) {
                // Optionally handle error
                view?.context?.let {
                    Toast.makeText(it, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                    Log.e("NetworkError", "Failed to update $device config: ${t.message}", t)
                }
            }
        })
    }

    private fun updateConfig(isSuhu: Boolean, value: Float, isPeriodic: Boolean) {
        val configRequest = ConfigRequest(
            period = null,
            ssid = null,
            password = null,
            is_periodic_sensor = isPeriodic,
            manual_fan = null,
            manual_heater = null,
            manual_humidifier = null,
            manual_humidity = if (!isSuhu && !isPeriodic) value.toInt() else 0,
            manual_temp = if (isSuhu && !isPeriodic) value.toInt() else 0
        )

        RetrofitClient.instance.postConfig(configRequest).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                // Optionally handle success
                if (response.isSuccessful) {
                    view?.context?.let {
                        Toast.makeText(it, "Konfigurasi berhasil diperbarui", Toast.LENGTH_SHORT)
                            .show()
                    }
                } else {
                    view?.context?.let {
                        Toast.makeText(it, "Gagal memperbarui konfigurasi", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                // Optionally handle error
                view?.context?.let {
                    Toast.makeText(it, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                    Log.e("NetworkError", "Failed to update config: ${t.message}", t)
                }
            }
        })
    }

    private fun fetchSensorData() {
        val apiService = RetrofitClient.instance.getLatestSensorData()
        apiService.enqueue(object : Callback<HistoryItemResponse> {
            override fun onResponse(
                call: Call<HistoryItemResponse>,
                response: Response<HistoryItemResponse>
            ) {
                currentSuhu = 0.toFloat() // Update currentSuhu
                currentKelembaban = 0.toFloat() // Update currentSuhu
                realSuhu = currentSuhu
                realKelembaban = currentSuhu

                if (response.isSuccessful) {
                    val sensorData = response.body()
                    Log.d("APIResponse", "Response body: $sensorData")

                    if (sensorData != null) {
                        // Update TextView values
                        currentSuhu = sensorData.temperature.toFloat() // Update currentSuhu
                        currentKelembaban = sensorData.humidity.toFloat() // Update currentSuhu
                        realSuhu = currentSuhu
                        realKelembaban = currentSuhu
                        suhuValue.text = "${currentSuhu}°C"
                        kelembapanValue.text = "${currentKelembaban}%"
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

            override fun onFailure(call: Call<HistoryItemResponse>, t: Throwable) {
                view?.context?.let {
                    Toast.makeText(it, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                    Log.e("NetworkError", "Failed to fetch data: ${t.message}", t)
                }
            }
        })
    }
}
