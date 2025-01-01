package com.monitoring.farmasidinkesminahasa.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.monitoring.farmasidinkesminahasa.R
import com.monitoring.farmasidinkesminahasa.adapter.ListViewAdapterTools
import com.monitoring.farmasidinkesminahasa.service.ToolService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ToolsFragment : Fragment() {

    private lateinit var adapter: ListViewAdapterTools
    private lateinit var listView: ListView
    private val toolService by lazy {
        Retrofit.Builder()
            .baseUrl("http://172.30.24.218:3000/") // Replace with your base URL
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ToolService::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_tools, container, false)

        listView = view.findViewById(R.id.listViewTools)

        // Fetch the tools data asynchronously
        fetchToolsData()

        return view
    }

    private fun fetchToolsData() {
        // Run the network request on a background thread
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = toolService.getTools()

                if (response.isSuccessful) {
                    // If the response is successful, extract the body
                    val toolsList = response.body()?.tools ?: emptyList()

                    withContext(Dispatchers.Main) {
                        if (toolsList.isEmpty()) {
                            // If no data, show "No data" message and hide ListView
                            listView.visibility = View.GONE
                            view?.findViewById<TextView>(R.id.noDataTextView)?.visibility = View.VISIBLE
                        } else {
                            // If there is data, populate the ListView
                            val listData = toolsList.map {
                                hashMapOf(
                                    "title" to it.name,
                                    "subTitle" to it.name,
                                    "description" to it.description,
                                    "paragraph" to it.description
                                )
                            }
                            adapter = ListViewAdapterTools(requireContext(), ArrayList(listData))
                            listView.adapter = adapter

                            // Show ListView and hide "No data" message
                            listView.visibility = View.VISIBLE
                            view?.findViewById<TextView>(R.id.noDataTextView)?.visibility = View.GONE
                        }
                    }
                } else {
                    // Handle errors in the response
                    Log.e("ToolsFragment", "Failed to load tools data: ${response.message()}")
                }
            } catch (e: Exception) {
                // Handle exception (network failure, etc.)
                Log.e("ToolsFragment", "Error: ${e.message}")
            }
        }
    }
}



