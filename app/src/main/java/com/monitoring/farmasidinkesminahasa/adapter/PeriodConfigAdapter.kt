package com.monitoring.farmasidinkesminahasa.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.monitoring.farmasidinkesminahasa.R
import com.monitoring.farmasidinkesminahasa.model.PeriodOption

class PeriodConfigAdapter(
    private val options: List<PeriodOption>,
    private val onItemSelected: (PeriodOption) -> Unit
) : RecyclerView.Adapter<PeriodConfigAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cardView: CardView = view.findViewById(R.id.cardView)
        val textView: TextView = view.findViewById(R.id.textView)

        fun bind(option: PeriodOption, position: Int) {
            textView.text = option.label

            // Ganti warna saat dipilih
            if (option.isSelected) {
                cardView.setCardBackgroundColor(Color.parseColor("#448AFF")) // Biru terang
                textView.setTextColor(Color.WHITE)
            } else {
                cardView.setCardBackgroundColor(Color.parseColor("#E0E0E0")) // Abu default
                textView.setTextColor(Color.BLACK)
            }

            cardView.setOnClickListener {
                // Reset semua pilihan
                options.forEach { it.isSelected = false }
                option.isSelected = true
                notifyDataSetChanged()

                onItemSelected(option)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_period_option, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(options[position], position)
    }

    override fun getItemCount(): Int = options.size
}
