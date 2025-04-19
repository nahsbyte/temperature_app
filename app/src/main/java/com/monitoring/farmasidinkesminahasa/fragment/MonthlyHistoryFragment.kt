package com.monitoring.farmasidinkesminahasa.fragment

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.StateListDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.monitoring.farmasidinkesminahasa.R
import com.monitoring.farmasidinkesminahasa.interfaces.CustomMarkerView
import com.monitoring.farmasidinkesminahasa.model.HistoryItemResponse
import com.monitoring.farmasidinkesminahasa.service.RetrofitClient
import jxl.Workbook
import jxl.write.Label
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MonthlyHistoryFragment : Fragment() {

    private lateinit var tvTitle: TextView
    private lateinit var label: String
    private lateinit var endDate: String
    private lateinit var startDate: String
    private lateinit var listView: ListView
    private lateinit var lineChart: LineChart
    private lateinit var rvPeriodOption: RecyclerView
    private lateinit var cv6: CardView
    private var allHistoryData: List<HistoryItemResponse>? =
        null
    private var selectedIndex: Int = -1  // Track the selected index from ListView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            label = it.getString("label").toString()
            startDate = it.getString("startDate").toString()
            endDate = it.getString("endDate").toString()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_monthly_history, container, false)

        listView = view.findViewById(R.id.listView)
        lineChart = view.findViewById(R.id.lineChart)
        tvTitle = view.findViewById(R.id.tvTitle)
        tvTitle.text = label
        cv6 = view.findViewById(R.id.cv6)
        cv6.setOnClickListener {
            Toast.makeText(context, "Clicked", Toast.LENGTH_SHORT).show()
            // Export history to csv

            exportSensorToExcelAndShowDialog(requireContext(), allHistoryData ?: emptyList(), label)
        }

        // Fetch data from API
        fetchDataFromApi()

        // Allow horizontal scrolling of the line chart
        lineChart.isDragEnabled = true
        lineChart.setScaleEnabled(true)

        // Set up the custom marker view
        setupMarkerView()

        return view
    }

    fun exportSensorToExcelAndShowDialog(
        context: Context,
        data: List<HistoryItemResponse>,
        label: String
    ) {
        try {
            var file: File? = null
            var uri: Uri? = null
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                uri = exportToDownloadFolderUri(context, data, label)
            } else {
                file = exportToDownloadFolderFile(context, data, label)
            }

            // Tampilkan dialog
            if (file != null) {
                showExportSuccessDialog(context, file, null)
            } else {
                showExportSuccessDialog(context, null, uri)
            }

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Gagal menyimpan file: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    fun exportToDownloadFolderFile(
        context: Context,
        data: List<HistoryItemResponse>,
        label: String
    ): File? {
        return exportToDownloadFolderUsingEnvironment(context, data, label)
    }

    fun exportToDownloadFolderUri(
        context: Context,
        data: List<HistoryItemResponse>,
        label: String
    ): Uri? {
        return exportToDownloadFolderUsingMediaStore(context, data, label)
    }

    // Untuk API 29+ (Android 10 dan lebih tinggi)
    fun exportToDownloadFolderUsingMediaStore(
        context: Context,
        data: List<HistoryItemResponse>,
        label: String
    ): Uri? {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd-HH-mm", Locale.getDefault())
            val calendar = Calendar.getInstance()

            // Set end date to today
            calendar.time = Date()
            val today = sdf.format(calendar.time)

            val fileName = "Laporan_Sensor_${label}_${today}.xls"
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "application/vnd.ms-excel")
                put(
                    MediaStore.MediaColumns.RELATIVE_PATH,
                    "Download/"
                )  // Simpan di folder 'Download'
            }

            val contentResolver = context.contentResolver
            val uri =
                contentResolver.insert(MediaStore.Files.getContentUri("external"), contentValues)

            uri?.let { fileUri ->
                contentResolver.openOutputStream(fileUri).use { outputStream ->
                    val workbook = Workbook.createWorkbook(outputStream)
                    val sheet = workbook.createSheet("Sensor", 0)

                    val headers = listOf("Tanggal", "Suhu (°C)", "Kelembaban (%)")
                    headers.forEachIndexed { index, header ->
                        sheet.addCell(Label(index, 0, header))
                    }

                    data.forEachIndexed { i, item ->
                        sheet.addCell(
                            Label(
                                0,
                                i + 1,
                                convertTimestampToDateTime(item.timestamp ?: 0)
                            )
                        )
                        sheet.addCell(Label(1, i + 1, item.temperature.toString()))
                        sheet.addCell(Label(2, i + 1, item.humidity.toString()))
                    }

                    workbook.write()
                    workbook.close()
                }
                return fileUri  // Kembalikan file URI sebagai File
            }
        } catch (e: IOException) {
            Log.e("File Export", "Error saving file: ${e.localizedMessage}")
            return null
        }
    }

    // Untuk API 28 (Android 9) dan sebelumnya
    fun exportToDownloadFolderUsingEnvironment(
        context: Context,
        data: List<HistoryItemResponse>,
        label: String
    ): File? {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd-HH-mm", Locale.getDefault())
            val calendar = Calendar.getInstance()

            // Set end date to today
            calendar.time = Date()
            val today = sdf.format(calendar.time)

            val fileName = "Laporan_Sensor_${label}_${today}.xls"
            val downloadsDir =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val file = File(downloadsDir, fileName)

            val workbook = Workbook.createWorkbook(file)
            val sheet = workbook.createSheet("Sensor", 0)

            val headers = listOf("Tanggal", "Suhu (°C)", "Kelembaban (%)")
            headers.forEachIndexed { index, header ->
                sheet.addCell(Label(index, 0, header))
            }

            data.forEachIndexed { i, item ->
                sheet.addCell(Label(0, i + 1, convertTimestampToDateTime(item.timestamp ?: 0)))
                sheet.addCell(Label(1, i + 1, item.temperature.toString()))
                sheet.addCell(Label(2, i + 1, item.humidity.toString()))
            }

            workbook.write()
            workbook.close()

            file  // Kembalikan File di folder Download
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun showExportSuccessDialog(context: Context, file: File?, uri: Uri?) {
        var path = ""
        if (file == null && uri == null) {
            Toast.makeText(context, "Gagal menyimpan file", Toast.LENGTH_LONG).show()
            return
        }else if (file != null){
            path = file.absolutePath
        }else if (uri != null){
            path = uri.toString()
        }

        AlertDialog.Builder(context)
            .setTitle("Berhasil")
            .setMessage("File disimpan di folder Download: ${path}")
            .setPositiveButton("Lihat File") { _, _ ->
                if (file != null) {
                    openFileFolder(context, file)
                }

                if (uri != null) {
                    openFileUri(context, uri)
                }
            }
            .setNegativeButton("Tutup", null)
            .show()
    }

    fun openFileFolder(context: Context, file: File) {
//        val downloads = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).path)
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file.absoluteFile
        )

        Log.d("FILE_PATH", file.absolutePath)

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "*/*")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Tidak dapat membuka folder Download", Toast.LENGTH_SHORT)
                .show()
        }
    }

    fun openFileUri(context: Context, uri: Uri) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.ms-excel")
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        }

        // Pastikan ada app yang bisa handle
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        } else {
            Toast.makeText(
                context,
                "Tidak ada aplikasi untuk membuka file Excel",
                Toast.LENGTH_SHORT
            ).show()
        }
    }


    private fun setupMarkerView() {
        val markerView = CustomMarkerView(requireContext(), R.layout.marker_view)
        lineChart.marker = markerView
    }

    private fun fetchDataFromApi() {
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

    private fun convertTimestampToDateTime(timestamp: Long): String {
        val date = Date(timestamp)
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
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