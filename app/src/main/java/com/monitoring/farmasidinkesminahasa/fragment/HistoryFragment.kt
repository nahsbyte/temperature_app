package com.monitoring.farmasidinkesminahasa.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet

import com.monitoring.farmasidinkesminahasa.R


class HistoryFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var view = inflater.inflate(com.monitoring.farmasidinkesminahasa.R.layout.fragment_history, container, false)

        // Setup ListView
        val listView = view.findViewById<ListView>(com.monitoring.farmasidinkesminahasa.R.id.listView)
        val listData = listData // This doesn't cause reassigning issues, as listData is just a reference to the method
        val adapter = ListViewAdapter(requireContext(), listData)
        listView.adapter = adapter

        // Setup LineChart
        val lineChart: LineChart = view.findViewById(com.monitoring.farmasidinkesminahasa.R.id.lineChart)
        setupLineChart(lineChart)

        return view
    }

    private val listData: ArrayList<HashMap<String, String>>
        get() {
            val list = ArrayList<HashMap<String, String>>()
            for (i in 1..8) {
                val row = HashMap<String, String>()
                row["header1"] = "Header 1, Row $i"
                row["header2"] = "Header 2, Row $i"
                row["header3"] = "Header 3, Row $i"
                row["header4"] = "Header 4, Row $i"
                list.add(row)
            }
            return list
        }

    private fun setupLineChart(lineChart: LineChart) {
        val entries1 = ArrayList<com.github.mikephil.charting.data.Entry>()
        entries1.add(com.github.mikephil.charting.data.Entry(0f, 5f))
        entries1.add(com.github.mikephil.charting.data.Entry(1f, 10f))
        entries1.add(com.github.mikephil.charting.data.Entry(2f, 7f))
        entries1.add(com.github.mikephil.charting.data.Entry(3f, 15f))
        entries1.add(com.github.mikephil.charting.data.Entry(4f, 12f))

        val dataSet1 = LineDataSet(entries1, "Data 1")
        dataSet1.color = ContextCompat.getColor(requireContext(), R.color.red) // Warna garis
        dataSet1.valueTextColor = ContextCompat.getColor(requireContext(), R.color.black) // Warna nilai
        dataSet1.lineWidth = 2f
        dataSet1.setDrawFilled(false) // Tidak mengisi area di bawah garis
        dataSet1.setDrawValues(false) // Tidak menampilkan nilai di atas titik
        dataSet1.mode = LineDataSet.Mode.CUBIC_BEZIER // Mode melengkung
        dataSet1.cubicIntensity = 0.2f // Intensitas kelengkungan (0-1, default: 0.2)

        val entries2 = ArrayList<com.github.mikephil.charting.data.Entry>()
        entries2.add(com.github.mikephil.charting.data.Entry(0f, 8f))
        entries2.add(com.github.mikephil.charting.data.Entry(1f, 12f))
        entries2.add(com.github.mikephil.charting.data.Entry(2f, 9f))
        entries2.add(com.github.mikephil.charting.data.Entry(3f, 18f))
        entries2.add(com.github.mikephil.charting.data.Entry(4f, 14f))

        val dataSet2 = LineDataSet(entries2, "Data 2")
        dataSet2.color = ContextCompat.getColor(requireContext(), R.color.blue) // Warna garis
        dataSet2.valueTextColor = ContextCompat.getColor(requireContext(), R.color.black) // Warna nilai
        dataSet2.lineWidth = 2f
        dataSet2.setDrawFilled(false) // Tidak mengisi area di bawah garis
        dataSet2.setDrawValues(false) // Tidak menampilkan nilai di atas titik
        dataSet2.mode = LineDataSet.Mode.CUBIC_BEZIER // Mode melengkung
        dataSet2.cubicIntensity = 0.2f // Intensitas kelengkungan (0-1, default: 0.2)

        val lineData = LineData(dataSet1, dataSet2)
        lineChart.data = lineData

        // Pengaturan sumbu X
        val xAxis = lineChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.granularity = 1f

        // Pengaturan sumbu Y kiri
        val yAxisLeft = lineChart.axisLeft
        yAxisLeft.axisMinimum = 0f

        // Nonaktifkan sumbu Y kanan
        val yAxisRight = lineChart.axisRight
        yAxisRight.isEnabled = false

        // Pengaturan grafik
        lineChart.description.isEnabled = false
        lineChart.legend.isEnabled = true
        lineChart.setTouchEnabled(true)
        lineChart.setPinchZoom(true)
        lineChart.setDrawGridBackground(false)
        lineChart.setExtraOffsets(10f, 10f, 10f, 10f)

        // Perbarui grafik
        lineChart.invalidate()
    }






}
