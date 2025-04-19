package com.monitoring.farmasidinkesminahasa.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.monitoring.farmasidinkesminahasa.R
import com.monitoring.farmasidinkesminahasa.model.ReportMenu

class ReportMenuAdapter(
    private val monthList: List<ReportMenu>,
    private val onClick: (ReportMenu) -> Unit
) : RecyclerView.Adapter<ReportMenuAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cardView: CardView = view.findViewById(R.id.cardView)
        val textView: TextView = view.findViewById(R.id.textView)

        fun bind(item: ReportMenu) {
            textView.text = item.label
            cardView.setOnClickListener {
                onClick(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_report_menu, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = monthList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(monthList[position])
    }
}