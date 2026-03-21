package com.example.signspeak

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.signspeak.Fragment.BookingDate
import com.example.signspeak.Fragment.DateAdapter
import com.example.signspeak.Fragment.TimeSlot
import com.example.signspeak.Fragment.TimeSlotAdapter

class BookAppointmentFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_book_appointment, container, false)

        val btnBack = view.findViewById<ImageView>(R.id.btnBack)
        btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        val rvDates = view.findViewById<RecyclerView>(R.id.rvDates)
        rvDates.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        
        val dates = listOf(
            BookingDate("Mon", "12"),
            BookingDate("Tue", "13"),
            BookingDate("Wed", "14"),
            BookingDate("Thu", "15"),
            BookingDate("Fri", "16"),
            BookingDate("Sat", "17")
        )
        rvDates.adapter = DateAdapter(dates)

        val rvTimeSlots = view.findViewById<RecyclerView>(R.id.rvTimeSlots)
        rvTimeSlots.layoutManager = GridLayoutManager(context, 3)

        val timeSlots = listOf(
            TimeSlot("09:00 AM"),
            TimeSlot("09:30 AM"),
            TimeSlot("10:00 AM"),
            TimeSlot("10:30 AM"),
            TimeSlot("11:00 AM"),
            TimeSlot("11:30 AM"),
            TimeSlot("02:00 PM"),
            TimeSlot("02:30 PM", isDisabled = true),
            TimeSlot("03:00 PM")
        )
        rvTimeSlots.adapter = TimeSlotAdapter(timeSlots)

        // Consultation Type Toggles
        val llVideoCall = view.findViewById<LinearLayout>(R.id.llVideoCall)
        val ivCheckVideo = view.findViewById<ImageView>(R.id.ivCheckVideo)
        val llInPerson = view.findViewById<LinearLayout>(R.id.llInPerson)
        val ivCheckInPerson = view.findViewById<ImageView>(R.id.ivCheckInPerson)

        llVideoCall.setOnClickListener {
            llVideoCall.setBackgroundResource(R.drawable.bg_med_consultation_card_selected)
            ivCheckVideo.visibility = View.VISIBLE
            
            llInPerson.setBackgroundResource(R.drawable.bg_med_consultation_card_normal)
            ivCheckInPerson.visibility = View.GONE
        }

        llInPerson.setOnClickListener {
            llInPerson.setBackgroundResource(R.drawable.bg_med_consultation_card_selected)
            ivCheckInPerson.visibility = View.VISIBLE
            
            llVideoCall.setBackgroundResource(R.drawable.bg_med_consultation_card_normal)
            ivCheckVideo.visibility = View.GONE
        }

        val btnConfirmAppointment = view.findViewById<Button>(R.id.btnConfirmAppointment)
        btnConfirmAppointment.setOnClickListener {
            Toast.makeText(context, "Appointment Confirmed!", Toast.LENGTH_SHORT).show()
            
            // Navigate to Appointment Summary or just pop back to Schedule!
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, MedicalAppointmentFragment())
                .commit()
        }

        return view
    }
}
