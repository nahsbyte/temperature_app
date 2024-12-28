package com.monitoring.farmasidinkesminahasa.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import androidx.fragment.app.Fragment
import com.monitoring.farmasidinkesminahasa.R
import com.monitoring.farmasidinkesminahasa.adapter.ListViewAdapterTools

class ToolsFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_tools, container, false)

        // Setup ListView
        val listView = view.findViewById<ListView>(R.id.listViewTools)
        val listData = getListData()
        val adapter = ListViewAdapterTools(requireContext(), listData)
        listView.adapter = adapter

        return view
    }

    private fun getListData(): ArrayList<HashMap<String, String>> {
        val list = ArrayList<HashMap<String, String>>()

        // Hardcoded dummy data for testing
        for (i in 1..5) {
            val row = HashMap<String, String>()
            row["title"] = "Title $i"
            row["subTitle"] = "Sub Title $i"
            row["description"] = "This is a description for item $i"
            row["paragraph"] = "Detailed paragraph for item $i. This is more information about the item."
            list.add(row)
        }

        return list
    }
}

