package com.example.signspeak.Fragment

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.signspeak.R

data class BookingDate(val day: String, val date: String)

class DateAdapter(private val list: List<BookingDate>) : RecyclerView.Adapter<DateAdapter.ViewHolder>() {
    private var selectedPosition = 1

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val llDateContainer: LinearLayout = view.findViewById(R.id.llDateContainer)
        val tvDay: TextView = view.findViewById(R.id.tvDay)
        val tvDate: TextView = view.findViewById(R.id.tvDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_date, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.tvDay.text = item.day
        holder.tvDate.text = item.date

        if (position == selectedPosition) {
            holder.llDateContainer.setBackgroundResource(R.drawable.bg_med_date_selected)
            holder.tvDay.setTextColor(Color.parseColor("#B3FFFFFF")) // slightly faded white
            holder.tvDate.setTextColor(Color.WHITE)
        } else {
            holder.llDateContainer.setBackgroundResource(R.drawable.bg_med_date_normal)
            holder.tvDay.setTextColor(Color.parseColor("#424754"))
            holder.tvDate.setTextColor(Color.parseColor("#191c1e"))
        }

        holder.itemView.setOnClickListener {
            val oldPos = selectedPosition
            selectedPosition = holder.adapterPosition
            notifyItemChanged(oldPos)
            notifyItemChanged(selectedPosition)
        }
    }

    override fun getItemCount() = list.size
}

data class TimeSlot(val time: String, val isDisabled: Boolean = false)

class TimeSlotAdapter(private val list: List<TimeSlot>) : RecyclerView.Adapter<TimeSlotAdapter.ViewHolder>() {
    private var selectedPosition = 2

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTimeSlot: TextView = view.findViewById(R.id.tvTimeSlot)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_time_slot, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.tvTimeSlot.text = item.time

        if (item.isDisabled) {
            holder.tvTimeSlot.setBackgroundResource(R.drawable.bg_med_time_normal)
            holder.tvTimeSlot.setTextColor(Color.parseColor("#424754"))
            holder.tvTimeSlot.alpha = 0.4f
            holder.itemView.isEnabled = false
        } else {
            holder.tvTimeSlot.alpha = 1.0f
            holder.itemView.isEnabled = true
            
            if (position == selectedPosition) {
                holder.tvTimeSlot.setBackgroundResource(R.drawable.bg_med_time_selected)
                holder.tvTimeSlot.setTextColor(Color.WHITE)
            } else {
                holder.tvTimeSlot.setBackgroundResource(R.drawable.bg_med_time_normal)
                holder.tvTimeSlot.setTextColor(Color.parseColor("#424754"))
            }

            holder.itemView.setOnClickListener {
                val oldPos = selectedPosition
                selectedPosition = holder.adapterPosition
                notifyItemChanged(oldPos)
                notifyItemChanged(selectedPosition)
            }
        }
    }

    override fun getItemCount() = list.size
}
