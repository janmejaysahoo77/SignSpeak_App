package com.example.signspeak.Fragment

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.signspeak.R

data class UpcomingAppointment(
    val doctorName: String,
    val specialty: String,
    val date: String,
    val time: String,
    val isVideoCall: Boolean,
    val isConfirmed: Boolean,
    val avatarResId: Int
)

data class BookingHistoryItem(
    val doctorName: String,
    val description: String,
    val iconResId: Int
)

class UpcomingAppointmentAdapter(private val list: List<UpcomingAppointment>) : RecyclerView.Adapter<UpcomingAppointmentAdapter.ViewHolder>() {
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivTypeIcon: ImageView = view.findViewById(R.id.ivTypeIcon)
        val tvAppointmentType: TextView = view.findViewById(R.id.tvAppointmentType)
        val tvStatusBadge: TextView = view.findViewById(R.id.tvStatusBadge)
        val ivDoctorAvatar: ImageView = view.findViewById(R.id.ivDoctorAvatar)
        val tvDoctorName: TextView = view.findViewById(R.id.tvDoctorName)
        val tvDoctorSpecialty: TextView = view.findViewById(R.id.tvDoctorSpecialty)
        val tvDate: TextView = view.findViewById(R.id.tvDate)
        val tvTime: TextView = view.findViewById(R.id.tvTime)
        val btnPrimaryAction: Button = view.findViewById(R.id.btnPrimaryAction)
        val btnSecondaryAction: Button = view.findViewById(R.id.btnSecondaryAction)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_upcoming_appointment, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.tvDoctorName.text = item.doctorName
        holder.tvDoctorSpecialty.text = item.specialty
        holder.tvDate.text = item.date
        holder.tvTime.text = item.time
        holder.ivDoctorAvatar.setImageResource(item.avatarResId)

        if (item.isVideoCall) {
            holder.ivTypeIcon.setImageResource(android.R.drawable.ic_menu_camera)
            holder.tvAppointmentType.text = "VIDEO CALL"
            holder.btnPrimaryAction.text = "Join Call"
            holder.btnSecondaryAction.text = "Reschedule"
        } else {
            holder.ivTypeIcon.setImageResource(android.R.drawable.ic_menu_myplaces)
            holder.tvAppointmentType.text = "IN-PERSON VISIT"
            holder.btnPrimaryAction.text = "Manage"
            holder.btnSecondaryAction.text = "Directions"
        }

        if (item.isConfirmed) {
            holder.tvStatusBadge.text = "CONFIRMED"
            holder.tvStatusBadge.setBackgroundResource(R.drawable.bg_med_badge_confirmed)
            holder.tvStatusBadge.setTextColor(Color.parseColor("#166534"))
        } else {
            holder.tvStatusBadge.text = "PENDING"
            holder.tvStatusBadge.setBackgroundResource(R.drawable.bg_med_badge_pending)
            holder.tvStatusBadge.setTextColor(Color.parseColor("#92400e"))
        }
    }

    override fun getItemCount() = list.size
}

class BookingHistoryAdapter(
    private val list: List<BookingHistoryItem>,
    private val onViewSummaryClick: (BookingHistoryItem) -> Unit
) : RecyclerView.Adapter<BookingHistoryAdapter.ViewHolder>() {
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivHistoryIcon: ImageView = view.findViewById(R.id.ivHistoryIcon)
        val tvHistoryDoctorName: TextView = view.findViewById(R.id.tvHistoryDoctorName)
        val tvHistoryDescription: TextView = view.findViewById(R.id.tvHistoryDescription)
        val btnViewSummary: TextView = view.findViewById(R.id.btnViewSummary)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_booking_history, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.tvHistoryDoctorName.text = item.doctorName
        holder.tvHistoryDescription.text = item.description
        holder.ivHistoryIcon.setImageResource(item.iconResId)
        
        holder.btnViewSummary.setOnClickListener {
            onViewSummaryClick(item)
        }
    }

    override fun getItemCount() = list.size
}
