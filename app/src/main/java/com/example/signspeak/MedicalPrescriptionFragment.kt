package com.example.signspeak

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.signspeak.Fragment.Prescription
import com.example.signspeak.Fragment.PrescriptionAdapter
import com.example.signspeak.Fragment.PrescriptionHistory
import com.example.signspeak.Fragment.PrescriptionHistoryAdapter

class MedicalPrescriptionFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_medical_prescription, container, false)

        val rvCurrentPrescriptions = view.findViewById<RecyclerView>(R.id.rvCurrentPrescriptions)
        rvCurrentPrescriptions.layoutManager = GridLayoutManager(context, 1)

        val prescriptions = listOf(
            Prescription(
                rxNumber = "#RX-99201",
                medicineName = "Amoxicillin 500mg",
                dosage = "1 pill twice daily after meals",
                doctorName = "Dr. Sarah Johnson",
                iconResId = android.R.drawable.ic_menu_add,
                iconBgResId = R.drawable.bg_med_icon_primary,
                isPrimaryAction = true
            ),
            Prescription(
                rxNumber = "#RX-88412",
                medicineName = "Lisinopril 10mg",
                dosage = "Once daily before breakfast",
                doctorName = "Dr. Michael Chen",
                iconResId = android.R.drawable.ic_menu_upload,
                iconBgResId = R.drawable.bg_med_icon_secondary,
                isPrimaryAction = false
            ),
            Prescription(
                rxNumber = "#RX-55210",
                medicineName = "Metformin 500mg",
                dosage = "Twice daily with meals",
                doctorName = "",
                iconResId = android.R.drawable.ic_menu_zoom,
                iconBgResId = R.drawable.bg_med_icon_tertiary,
                isPrimaryAction = false,
                showSupplyWarning = true,
                supplyProgress = 15,
                supplyWarningText = "Refill needed within 4 days"
            )
        )
        rvCurrentPrescriptions.adapter = PrescriptionAdapter(prescriptions)

        val rvPrescriptionArchive = view.findViewById<RecyclerView>(R.id.rvPrescriptionArchive)
        rvPrescriptionArchive.layoutManager = LinearLayoutManager(context)

        val historyList = listOf(
            PrescriptionHistory("Azithromycin 250mg", "Treatment ended Oct 2023"),
            PrescriptionHistory("Prednisone 5mg", "Treatment ended Aug 2023")
        )
        rvPrescriptionArchive.adapter = PrescriptionHistoryAdapter(historyList)

        return view
    }
}
