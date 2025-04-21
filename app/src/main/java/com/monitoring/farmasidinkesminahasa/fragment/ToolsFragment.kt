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
import com.monitoring.farmasidinkesminahasa.model.Tool
import com.monitoring.farmasidinkesminahasa.service.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ToolsFragment : Fragment() {

    private lateinit var adapter: ListViewAdapterTools
    private lateinit var listView: ListView

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
        try {
            // Create a Retrofit client
            val service = RetrofitClient.instance.getTools()

            service.enqueue(object : Callback<List<Tool>> {
                override fun onResponse(
                    call: Call<List<Tool>>,
                    response: Response<List<Tool>>
                ) {
                    if (response.isSuccessful) {
                        val tools = response.body()
                        if (tools != null) {
                            // If the response is successful, extract the body
                            val toolsList = response.body() ?: emptyList()

                            view?.context?.let {
                                if (toolsList.isEmpty()) {
                                    // If no data, show "No data" message and hide ListView
                                    listView.visibility = View.GONE
                                    view?.findViewById<TextView>(R.id.noDataTextView)?.visibility =
                                        View.VISIBLE
                                } else {
                                    // If there is data, populate the ListView
                                    val listData = arrayListOf(
                                        hashMapOf(
                                            "title" to "Judul",
                                            "description" to "Deskripsi singkat",
                                            "imageUrl" to "https://example.com/image.jpg"
                                        )
                                    )
                                    adapter = ListViewAdapterTools(requireContext(), ArrayList(toolsList))
                                    listView.adapter = adapter

                                    // Show ListView and hide "No data" message
                                    listView.visibility = View.VISIBLE
                                    view?.findViewById<TextView>(R.id.noDataTextView)?.visibility =
                                        View.GONE
                                }
                            }
                        }
                    } else {
                        Log.e("HistoryFragment", "Response failed: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<List<Tool>>, t: Throwable) {
                    Log.e("ToolsFragment", "API call failed: ${t.message}")
                }
            })
        } catch (e: Exception) {
            // Handle exception (network failure, etc.)
            Log.e("ToolsFragment", "Error: ${e.message}")
        }
    }
}



