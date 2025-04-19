package com.monitoring.farmasidinkesminahasa.fragment

import android.content.Context
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
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.monitoring.farmasidinkesminahasa.R
import com.monitoring.farmasidinkesminahasa.adapter.PeriodConfigAdapter
import com.monitoring.farmasidinkesminahasa.interfaces.CustomMarkerView
import com.monitoring.farmasidinkesminahasa.model.HistoryItemResponse
import com.monitoring.farmasidinkesminahasa.model.PeriodConfigRequest
import com.monitoring.farmasidinkesminahasa.model.PeriodOption
import com.monitoring.farmasidinkesminahasa.service.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class HistoryFragment : Fragment() {

    private lateinit var listView: ListView
    private lateinit var lineChart: LineChart
    private lateinit var rvPeriodOption: RecyclerView
    private var allHistoryData: List<com.monitoring.farmasidinkesminahasa.model.HistoryItemResponse>? =
        null
    private var selectedIndex: Int = -1  // Track the selected index from ListView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_history, container, false)

        listView = view.findViewById(R.id.listView)
        lineChart = view.findViewById(R.id.lineChart)
        rvPeriodOption = view.findViewById(R.id.rvPeriodOption)

        setupPeriodOption(requireContext())

        // Fetch data from API
        fetchDataFromApi()

        // Allow horizontal scrolling of the line chart
        lineChart.isDragEnabled = true
        lineChart.setScaleEnabled(true)

        // Set up the custom marker view
        setupMarkerView()

        return view
    }

    private fun setupPeriodOption(context: Context) {
        val options = listOf(
            PeriodOption("5 detik"),
            PeriodOption("1 Menit"),
            PeriodOption("5 Menit"),
            PeriodOption("10 Menit"),
            PeriodOption("1 Jam")
        )
        var periodConfigRequest = PeriodConfigRequest(period = 5000)


        val adapter = PeriodConfigAdapter(options) { selectedOption ->
            Toast.makeText(context, "Dipilih: ${selectedOption.label}", Toast.LENGTH_SHORT).show()
            // Lakukan POST request di sini jika perlu

            if (selectedOption.label == "5 detik") {
                periodConfigRequest = PeriodConfigRequest(period = 5000)
            } else if (selectedOption.label == "1 Menit") {
                periodConfigRequest = PeriodConfigRequest(period = 60000)
            } else if (selectedOption.label == "5 Menit") {
                periodConfigRequest = PeriodConfigRequest(period = 300000)
            } else if (selectedOption.label == "10 Menit") {
                periodConfigRequest = PeriodConfigRequest(period = 600000)
            } else if (selectedOption.label == "1 Jam") {
                periodConfigRequest = PeriodConfigRequest(period = 3600000)
            }

            val call = RetrofitClient.instance.postPeriodConfig(periodConfigRequest)

            call.enqueue(object : Callback<Void>{
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    Log.d("HistoryFragment", "API Response: ${response.body()}")
                }

                override fun onFailure(call: Call<Void>, response: Throwable) {
                    Log.e("HistoryFragment", "Network Failure: ${response.message}")
                }
            })
        }

        rvPeriodOption.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        rvPeriodOption.adapter = adapter

    }

    private fun setupMarkerView() {
        val markerView = CustomMarkerView(requireContext(), R.layout.marker_view)
        lineChart.marker = markerView
    }

    private fun fetchDataFromApi() {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val calendar = Calendar.getInstance()

        // Set start date to the 1st day of current month
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        val startDate = sdf.format(calendar.time)

        // Set end date to today
        calendar.time = Date()
        val endDate = sdf.format(calendar.time)

        val call = RetrofitClient.instance.getHistorySensorData(startDate, endDate)

        call.enqueue(object : Callback<List<HistoryItemResponse>> {
            override fun onResponse(
                call: Call<List<HistoryItemResponse>>,
                response: Response<List<HistoryItemResponse>>
            ) {
                if (response.isSuccessful) {
                    val data = response.body()
                    data?.let {
                        allHistoryData = it
                        updateListView(it)
                        updateLineChart(it)
                    }
                } else {
                    Log.e("HistoryFragment", "API Error: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<List<HistoryItemResponse>>, t: Throwable) {
                Log.e("HistoryFragment", "Network Failure: ${t.message}")
            }
        })
    }

    private fun updateListView(history: List<HistoryItemResponse>) {
        val adapter = object : ArrayAdapter<HistoryItemResponse>(
            requireContext(),
            R.layout.list_item_history,
            history
        ) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = convertView ?: LayoutInflater.from(context)
                    .inflate(R.layout.list_item_history, parent, false)

                val item = getItem(position)

                // Get the TextViews from the layout
                val dateView = view.findViewById<TextView>(R.id.date)
                val timeView = view.findViewById<TextView>(R.id.time)
                val humidityView = view.findViewById<TextView>(R.id.humidity)
                val temperatureView = view.findViewById<TextView>(R.id.temperature)

                // Format timestamp
                val formattedDate = convertTimestampToDate(item?.timestamp ?: 0)
                val formattedTime = convertTimestampToTime(item?.timestamp ?: 0)

                // Set the data into TextViews
                dateView.text = "$formattedDate"
                timeView.text = "$formattedTime"
                humidityView.text = "${item?.humidity}%"
                temperatureView.text = "${item?.temperature}°C"

                // Set up a clickable background effect
                val drawable = StateListDrawable().apply {
                    addState(
                        intArrayOf(android.R.attr.state_pressed),
                        ColorDrawable(Color.LTGRAY)
                    )
                    addState(intArrayOf(), ColorDrawable(Color.WHITE))
                }
                view.background = drawable

                return view
            }
        }

        listView.adapter = adapter

        listView.setOnItemClickListener { _, _, position, _ ->
            selectedIndex = position  // Update selected index
            allHistoryData?.let {
                // Update the line chart with the selected data
                updateLineChart(it)
                // Highlight the point on the chart based on ListView click
                highlightPointOnChart(position)
            }
        }
    }

    private fun convertTimestampToDate(timestamp: Long): String {
        val date = Date(timestamp)
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(date)
    }

    private fun convertTimestampToTime(timestamp: Long): String {
        val date = Date(timestamp)
        val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        return timeFormat.format(date)
    }

    private fun updateLineChart(history: List<HistoryItemResponse>) {
        if (history.isEmpty()) {
            lineChart.clear()
            lineChart.setNoDataText("No data available")
            lineChart.invalidate()
            return
        }

        val suhuEntries = ArrayList<Entry>()
        val kelembabanEntries = ArrayList<Entry>()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

        history.forEachIndexed { index, item ->
            val timestampDate = Date(item.timestamp) // Directly using the timestamp if it's epoch
            // Use SimpleDateFormat for date and time formatting
//            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) // For date
//            val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault()) // For time

            // Format the date and time separately
            val formattedDate = dateFormat.format(timestampDate)

//            val timestampDate = dateFormat.parse(item.timestamp.toString())
            formattedDate?.let {
                suhuEntries.add(Entry(index.toFloat(), item.temperature.toFloat()))
                kelembabanEntries.add(Entry(index.toFloat(), item.humidity.toFloat()))
            }
        }

        // Suhu Data Set
        val suhuDataSet = LineDataSet(suhuEntries, "Suhu").apply {
            color =
                ContextCompat.getColor(requireContext(), R.color.red) // Red color for temperature
            setDrawCircles(false)  // Initially do not draw circles
            circleRadius = 4f
            setDrawValues(false)
            lineWidth = 2f
            mode = LineDataSet.Mode.CUBIC_BEZIER
        }

        // Kelembaban Data Set
        val kelembabanDataSet = LineDataSet(kelembabanEntries, "Kelembaban").apply {
            color =
                ContextCompat.getColor(requireContext(), R.color.blue) // Blue color for humidity
            setDrawCircles(false)  // Initially do not draw circles
            circleRadius = 4f
            setDrawValues(false)
            lineWidth = 2f
            mode = LineDataSet.Mode.CUBIC_BEZIER
        }

        // If an item was clicked, set circles only on the selected point
        if (selectedIndex != -1) {
            // Highlight the circle for the selected index
            suhuDataSet.apply {
                setDrawCircles(true)  // Enable circles for the selected index
                if (selectedIndex != -1) {
                    val selectedPoint = suhuEntries[selectedIndex]
                    selectedPoint.apply {
                        setCircleColor(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.red
                            )
                        ) // Highlight with yellow color
                        circleRadius = 4f
                        mode = LineDataSet.Mode.CUBIC_BEZIER
                        lineWidth = 2f


                    }
                }
            }

            kelembabanDataSet.apply {
                setDrawCircles(true)  // Enable circles for the selected index
                if (selectedIndex != -1) {
                    val selectedPoint = kelembabanEntries[selectedIndex]
                    selectedPoint.apply {
                        setCircleColor(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.blue
                            )
                        ) // Highlight with yellow color
                        circleRadius = 4f
                        mode = LineDataSet.Mode.CUBIC_BEZIER
                        lineWidth = 2f


                    }
                }
            }
        }

        val lineData = LineData(suhuDataSet, kelembabanDataSet)

        val xAxis = lineChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.textColor = Color.GRAY
        xAxis.textSize = 12f
        xAxis.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return value.toInt().toString()
            }
        }

        lineChart.apply {
            data = lineData
            description.isEnabled = false
            legend.isEnabled = true
            setDrawBorders(false)
            setTouchEnabled(true)
            setPinchZoom(true)
            invalidate()
        }
    }

    private fun highlightPointOnChart(position: Int) {
        allHistoryData?.let { history ->
            val selectedItem = history[position]
            val timestamp = selectedItem.timestamp

            // Create a highlight for the selected point
            val highlight = Highlight(position.toFloat(), selectedItem.temperature.toFloat(), 0)

            // Highlight the specific point in the chart
            lineChart.highlightValue(highlight, true)

            // Update the marker with the Timestamp
            val markerView = lineChart.marker as CustomMarkerView
            markerView.updateTimestamp(timestamp.toString())

            lineChart.invalidate()  // Refresh the chart to show the selected point
        }
    }
}