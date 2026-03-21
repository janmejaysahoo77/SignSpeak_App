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

data class Specialty(val name: String, val isActive: Boolean)
data class Doctor(
    val name: String,
    val specialty: String,
    val rating: String,
    val reviews: String,
    val isAvailable: Boolean,
    val imageResId: Int
)

class SpecialtyAdapter(private val list: List<Specialty>) : RecyclerView.Adapter<SpecialtyAdapter.ViewHolder>() {
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvSpecialtyChip: TextView = view.findViewById(R.id.tvSpecialtyChip)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_specialty_chip, parent, false)
        return ViewHolder(view)
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.tvSpecialtyChip.text = item.name
        if (item.isActive) {
            holder.tvSpecialtyChip.setBackgroundResource(R.drawable.bg_specialty_chip_active)
            holder.tvSpecialtyChip.setTextColor(Color.WHITE)
        } else {
            holder.tvSpecialtyChip.setBackgroundResource(R.drawable.bg_specialty_chip_inactive)
            holder.tvSpecialtyChip.setTextColor(Color.parseColor("#424754"))
        }
    }
    override fun getItemCount() = list.size
}

class DoctorAdapter(
    private val list: List<Doctor>,
    private val onBookClick: (Doctor) -> Unit = {}
) : RecyclerView.Adapter<DoctorAdapter.ViewHolder>() {
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvDoctorName: TextView = view.findViewById(R.id.tvDoctorName)
        val tvSpecialty: TextView = view.findViewById(R.id.tvSpecialty)
        val tvRating: TextView = view.findViewById(R.id.tvRating)
        val tvReviews: TextView = view.findViewById(R.id.tvReviews)
        val tvStatusBadge: TextView = view.findViewById(R.id.tvStatusBadge)
        val btnAction: Button = view.findViewById(R.id.btnAction)
        val vOnlineIndicator: View = view.findViewById(R.id.vOnlineIndicator)
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_doctor, parent, false)
        return ViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.tvDoctorName.text = item.name
        holder.tvSpecialty.text = item.specialty
        holder.tvRating.text = item.rating
        holder.tvReviews.text = item.reviews

        if (item.isAvailable) {
            holder.tvStatusBadge.text = "AVAILABLE"
            holder.tvStatusBadge.setTextColor(Color.parseColor("#16a34a"))
            holder.tvStatusBadge.setBackgroundColor(Color.parseColor("#e6f4ea"))
            holder.vOnlineIndicator.setBackgroundColor(Color.parseColor("#22c55e"))
            holder.btnAction.text = "Book Now"
            holder.btnAction.setBackgroundResource(R.drawable.bg_doctor_btn_available)
            holder.btnAction.setOnClickListener { onBookClick(item) }
        } else {
            holder.tvStatusBadge.text = "BUSY"
            holder.tvStatusBadge.setTextColor(Color.parseColor("#dc2626"))
            holder.tvStatusBadge.setBackgroundColor(Color.parseColor("#fef2f2"))
            holder.vOnlineIndicator.setBackgroundColor(Color.parseColor("#ef4444"))
            holder.btnAction.text = "Next Available: 2 PM"
            holder.btnAction.setBackgroundResource(R.drawable.bg_doctor_btn_busy)
            holder.btnAction.setTextColor(Color.parseColor("#424754"))
            holder.btnAction.setOnClickListener(null)
        }
    }
    
    override fun getItemCount() = list.size
}
