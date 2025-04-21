package com.monitoring.farmasidinkesminahasa.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.monitoring.farmasidinkesminahasa.R
import com.monitoring.farmasidinkesminahasa.model.Tool

class ListViewAdapterTools(private val context: Context, private val dataList: ArrayList<Tool>) : BaseAdapter() {

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
        val descriptionText = view.findViewById<TextView>(R.id.description)

        // Load image from URL (if available), fallback to default
        val imageUrl = item.imageUrl
        if (!imageUrl.isNullOrEmpty()) {
            Glide.with(context)
                .load(imageUrl)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_menu_report_image)
                .into(imageView)
        } else {
            imageView.setImageResource(android.R.drawable.ic_menu_camera) // Fallback image
        }

        // Set the title, subtitle, description, and paragraph
        titleText.text = item.name
        descriptionText.text = item.description

        return view
    }
}

