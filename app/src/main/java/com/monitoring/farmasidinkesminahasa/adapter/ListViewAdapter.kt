package com.monitoring.farmasidinkesminahasa.fragment

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.monitoring.farmasidinkesminahasa.R

class ListViewAdapter(
    private val context: Context,
    private val data: ArrayList<HashMap<String, String>>
) :
    BaseAdapter() {
    override fun getCount(): Int {
        return data.size
    }

    override fun getItem(position: Int): Any {
        return data[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var view = convertView
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.row_item, parent, false)
        }

        val row = data[position]
        (view!!.findViewById<TextView>(R.id.header1)).text = row["header1"]
        (view.findViewById<TextView>(R.id.header2)).text = row["header2"]
        (view.findViewById<TextView>(R.id.header3)).text = row["header3"]
        (view.findViewById<TextView>(R.id.header4)).text = row["header4"]
        (view.findViewById<TextView>(R.id.header4)).text = row["header4"]
        (view.findViewById<TextView>(R.id.header4)).text = row["header4"]
        (view.findViewById<TextView>(R.id.header4)).text = row["header4"]
        (view.findViewById<TextView>(R.id.header4)).text = row["header4"]
        (view.findViewById<TextView>(R.id.header4)).text = row["header4"]
        (view.findViewById<TextView>(R.id.header4)).text = row["header4"]


        return view
    }

}