package com.monitoring.farmasidinkesminahasa.fragment

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
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
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.monitoring.farmasidinkesminahasa.R
import com.monitoring.farmasidinkesminahasa.model.SensorResponse
import com.monitoring.farmasidinkesminahasa.service.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class HistoryFragment : Fragment() {

    private lateinit var listView: ListView
    private lateinit var lineChart: LineChart
    private var allHistoryData: List<com.monitoring.farmasidinkesminahasa.model.HistoryItem>? = null
    private var selectedIndex: Int = -1  // Track the selected index from ListView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_history, container, false)

        listView = view.findViewById(R.id.listView)
        lineChart = view.findViewById(R.id.lineChart)

        // Fetch data from API
        fetchDataFromApi()

        // Allow horizontal scrolling of the line chart
        lineChart.isDragEnabled = true
        lineChart.setScaleEnabled(true)

        // Set up the custom marker view
        setupMarkerView()

        return view
    }

    private fun setupMarkerView() {
        val markerView = CustomMarkerView(requireContext(), R.layout.marker_view)
        lineChart.marker = markerView
    }

    private fun fetchDataFromApi() {
        val service = RetrofitClient.instance.getSensorData()

        service.enqueue(object : Callback<SensorResponse> {
            override fun onResponse(call: Call<SensorResponse>, response: Response<SensorResponse>) {
                if (response.isSuccessful) {
                    val sensorResponse = response.body()
                    sensorResponse?.let {
                        allHistoryData = it.History
                        updateListView(it.History)
                        updateLineChart(it.History)
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

        val adapter = object : ArrayAdapter<String>(requireContext(), android.R.layout.simple_list_item_1, listData) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent)
                val textView = view.findViewById<TextView>(android.R.id.text1)
                textView.setTextColor(Color.BLACK)
                textView.textSize = 16f
                textView.setPadding(16, 16, 16, 16)

                val drawable = StateListDrawable()
                drawable.addState(intArrayOf(android.R.attr.state_pressed), ColorDrawable(Color.LTGRAY))
                drawable.addState(intArrayOf(), ColorDrawable(Color.WHITE))
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

    private fun updateLineChart(history: List<com.monitoring.farmasidinkesminahasa.model.HistoryItem>) {
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
            val timestampDate = dateFormat.parse(item.Timestamp)
            timestampDate?.let {
                val timestamp = (it.time / 1000).toFloat()
                suhuEntries.add(Entry(index.toFloat(), item.Suhu.toFloat()))
                kelembabanEntries.add(Entry(index.toFloat(), item.Kelembaban.toFloat()))
            }
        }

        // Suhu Data Set
        val suhuDataSet = LineDataSet(suhuEntries, "Suhu").apply {
            color = ContextCompat.getColor(requireContext(), R.color.red) // Red color for temperature
            setDrawCircles(false)  // Initially do not draw circles
            circleRadius = 4f
            setDrawValues(false)
            lineWidth = 2f
            mode = LineDataSet.Mode.CUBIC_BEZIER
        }

        // Kelembaban Data Set
        val kelembabanDataSet = LineDataSet(kelembabanEntries, "Kelembaban").apply {
            color = ContextCompat.getColor(requireContext(), R.color.blue) // Blue color for humidity
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
                        setCircleColor(ContextCompat.getColor(requireContext(), R.color.red)) // Highlight with yellow color
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
                        setCircleColor(ContextCompat.getColor(requireContext(), R.color.blue)) // Highlight with yellow color
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
            val timestamp = selectedItem.Timestamp

            // Create a highlight for the selected point
            val highlight = Highlight(position.toFloat(), selectedItem.Suhu.toFloat(), 0)

            // Highlight the specific point in the chart
            lineChart.highlightValue(highlight, true)

            // Update the marker with the Timestamp
            val markerView = lineChart.marker as CustomMarkerView
            markerView.updateTimestamp(timestamp)

            lineChart.invalidate()  // Refresh the chart to show the selected point
        }
    }

}

// Custom Marker View for Highlighting
class CustomMarkerView(context: Context, layoutResource: Int) : MarkerView(context, layoutResource){

    private val paint = Paint().apply {
        color = Color.YELLOW
        strokeWidth = 3f
        isAntiAlias = true
    }

    // Add a reference to the TextView for displaying the Timestamp
    private val timestampTextView: TextView = findViewById(R.id.markerText)

    override fun draw(canvas: Canvas, posX: Float, posY: Float) {
        super.draw(canvas, posX, posY)

        // Ensure the types are compatible
        val startY = 0f
        val endY = height.toFloat()

        // Draw a vertical yellow line at the highlighted position (posX)
        canvas.drawLine(posX, startY, posX, endY, paint)
    }

    // Update the Timestamp in the marker's TextView
    fun updateTimestamp(timestamp: String) {
        timestampTextView.text = timestamp
    }
}