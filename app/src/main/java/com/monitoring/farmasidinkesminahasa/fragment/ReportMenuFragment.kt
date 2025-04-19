package com.monitoring.farmasidinkesminahasa.fragment

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.monitoring.farmasidinkesminahasa.R
import com.monitoring.farmasidinkesminahasa.adapter.ReportMenuAdapter
import com.monitoring.farmasidinkesminahasa.model.ReportMenu
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Calendar
import java.util.Locale

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ReportMenuFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ReportMenuFragment : Fragment() {
    private lateinit var rvReportMenu: RecyclerView

    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_report_menu, container, false)

        rvReportMenu = view.findViewById(R.id.rvReportMenu)
        setupReportMenu()

        return view
    }

    fun generateMonthList(): List<ReportMenu> {
        val locale = Locale("id", "ID")

        return (1..12).map { month ->
            val year = 2025

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                val start = LocalDate.of(year, month, 1)
                val end = start.withDayOfMonth(start.lengthOfMonth())
                val label =
                    "${start.month.getDisplayName(TextStyle.FULL, locale).uppercase()} $year"

                ReportMenu(label, start.format(formatter), end.format(formatter))
            } else {
                val calendar = Calendar.getInstance()
                calendar.set(year, month - 1, 1)

                val simpleFormat = SimpleDateFormat("yyyy-MM-dd", locale)
                val startDate = simpleFormat.format(calendar.time)

                calendar.set(
                    Calendar.DAY_OF_MONTH,
                    calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
                )
                val endDate = simpleFormat.format(calendar.time)

                val monthName = SimpleDateFormat("MMMM", locale).format(calendar.time).uppercase()

                ReportMenu("$monthName $year", startDate, endDate)
            }
        }
    }


    private fun setupReportMenu() {
        val adapter = ReportMenuAdapter(generateMonthList()) { selectedMonth ->
            Log.d("SELECTED", "Start: ${selectedMonth.startDate}, End: ${selectedMonth.endDate}")

            // Kirim ke HistoryDataFragment, atau panggil API:
//            val call = RetrofitClient.instance.postPeriodConfig(periodConfigRequest)
//
//            apiService.getHistorySensorData(
//                start = selectedMonth.startDate,
//                end = selectedMonth.endDate
//            )
        }
        rvReportMenu.layoutManager = LinearLayoutManager(requireContext())
        rvReportMenu.adapter = adapter
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ReportMenuFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ReportMenuFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}