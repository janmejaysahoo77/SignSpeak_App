package com.example.signspeak.Fragment

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.signspeak.R

data class Prescription(
    val rxNumber: String,
    val medicineName: String,
    val dosage: String,
    val doctorName: String,
    val iconResId: Int,
    val iconBgResId: Int,
    val isPrimaryAction: Boolean,
    val showSupplyWarning: Boolean = false,
    val supplyProgress: Int = 0,
    val supplyWarningText: String = ""
)

data class PrescriptionHistory(
    val medicineName: String,
    val subtext: String
)

class PrescriptionAdapter(private val list: List<Prescription>) : RecyclerView.Adapter<PrescriptionAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val flIconContainer: FrameLayout = view.findViewById(R.id.flIconContainer)
        val ivMedicineIcon: ImageView = view.findViewById(R.id.ivMedicineIcon)
        val tvRxNumber: TextView = view.findViewById(R.id.tvRxNumber)
        val tvMedicineName: TextView = view.findViewById(R.id.tvMedicineName)
        val tvDosage: TextView = view.findViewById(R.id.tvDosage)
        val tvDoctorName: TextView = view.findViewById(R.id.tvDoctorName)
        val llDoctorInfo: LinearLayout = view.findViewById(R.id.llDoctorInfo)
        
        val llSupplyBox: LinearLayout = view.findViewById(R.id.llSupplyBox)
        val pbSupply: ProgressBar = view.findViewById(R.id.pbSupply)
        val btnRefill: Button = view.findViewById(R.id.btnRefill)
        val btnSchedule: Button = view.findViewById(R.id.btnSchedule)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_prescription_card, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        
        holder.flIconContainer.setBackgroundResource(item.iconBgResId)
        holder.ivMedicineIcon.setImageResource(item.iconResId)
        holder.tvRxNumber.text = item.rxNumber
        holder.tvMedicineName.text = item.medicineName
        holder.tvDosage.text = item.dosage
        holder.tvDoctorName.text = item.doctorName

        if (item.showSupplyWarning) {
            holder.llDoctorInfo.visibility = View.GONE
            holder.llSupplyBox.visibility = View.VISIBLE
            holder.pbSupply.progress = item.supplyProgress
            
            val warningText = holder.llSupplyBox.getChildAt(3) as? TextView
            warningText?.text = item.supplyWarningText
            
            holder.btnRefill.visibility = View.GONE
            holder.btnSchedule.visibility = View.GONE
        } else {
            holder.llDoctorInfo.visibility = View.VISIBLE
            holder.llSupplyBox.visibility = View.GONE
            
            if (item.isPrimaryAction) {
                holder.btnRefill.visibility = View.VISIBLE
                holder.btnSchedule.visibility = View.GONE
            } else {
                holder.btnRefill.visibility = View.GONE
                holder.btnSchedule.visibility = View.VISIBLE
            }
        }
    }

    override fun getItemCount() = list.size
}

class PrescriptionHistoryAdapter(private val list: List<PrescriptionHistory>) : RecyclerView.Adapter<PrescriptionHistoryAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvHistoryMedName: TextView = view.findViewById(R.id.tvHistoryMedName)
        val tvHistorySubtext: TextView = view.findViewById(R.id.tvHistorySubtext)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_prescription_history, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.tvHistoryMedName.text = item.medicineName
        holder.tvHistorySubtext.text = item.subtext
    }

    override fun getItemCount() = list.size
}
