package com.monitoring.farmasidinkesminahasa.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.monitoring.farmasidinkesminahasa.R

class ListViewAdapterTools(private val context: Context, private val dataList: ArrayList<HashMap<String, String>>) : BaseAdapter() {

    override fun getCount(): Int {
        return dataList.size
    }

    override fun getItem(position: Int): Any {
        return dataList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View = convertView ?: LayoutInflater.from(context).inflate(R.layout.list_item, parent, false)

        val item = dataList[position]

        val imageView = view.findViewById<ImageView>(R.id.imageView)
        val titleText = view.findViewById<TextView>(R.id.title)
        val subTitleText = view.findViewById<TextView>(R.id.subTitle)
        val descriptionText = view.findViewById<TextView>(R.id.description)
        val paragraphText = view.findViewById<TextView>(R.id.paragraph)

        // Set dummy image (using a system image for testing)
        imageView.setImageResource(android.R.drawable.ic_menu_camera) // Dummy system image

        // Set the title, subtitle, description, and paragraph from the dummy data
        titleText.text = item["title"]
        subTitleText.text = item["subTitle"]
        descriptionText.text = item["description"]
        paragraphText.text = item["paragraph"]

        return view
    }
}

