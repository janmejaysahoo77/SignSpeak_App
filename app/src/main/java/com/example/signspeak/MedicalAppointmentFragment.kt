package com.example.signspeak

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.signspeak.Fragment.BookingHistoryAdapter
import com.example.signspeak.Fragment.BookingHistoryItem
import com.example.signspeak.Fragment.UpcomingAppointment
import com.example.signspeak.Fragment.UpcomingAppointmentAdapter

class MedicalAppointmentFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_medical_appointment, container, false)
        
        val rvUpcomingAppointments = view.findViewById<RecyclerView>(R.id.rvUpcomingAppointments)
        rvUpcomingAppointments.layoutManager = LinearLayoutManager(context)
        rvUpcomingAppointments.adapter = UpcomingAppointmentAdapter(listOf(
            UpcomingAppointment("Dr. Julian Vance", "Neurosurgeon • Specialist Clinic", "Nov 14, 2023", "10:00 AM", true, true, R.mipmap.ic_launcher),
            UpcomingAppointment("Dr. Sarah Chen", "Cardiologist • Heart Center", "Nov 18, 2023", "02:30 PM", false, false, R.mipmap.ic_launcher)
        ))

        val rvBookingHistory = view.findViewById<RecyclerView>(R.id.rvBookingHistory)
        rvBookingHistory.layoutManager = LinearLayoutManager(context)
        rvBookingHistory.adapter = BookingHistoryAdapter(listOf(
            BookingHistoryItem("Dr. Marcus Thorne", "General Checkup • Oct 12, 2023", android.R.drawable.ic_menu_add),
            BookingHistoryItem("Dr. Elena Rodriguez", "Dental Cleaning • Sep 25, 2023", android.R.drawable.ic_menu_agenda),
            BookingHistoryItem("Dr. Simon Kovic", "Consultation • Sep 02, 2023", android.R.drawable.ic_menu_info_details)
        )) {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ViewSummaryFragment())
                .addToBackStack(null)
                .commit()
        }

        return view
    }
}
