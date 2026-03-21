package com.example.signspeak.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.example.signspeak.R

class DoctorsFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_doctors, container, false)
        
        val rvSpecialties = view.findViewById<RecyclerView>(R.id.rvSpecialties)
        rvSpecialties.adapter = SpecialtyAdapter(listOf(
            Specialty("All", true),
            Specialty("General", false),
            Specialty("Dentist", false),
            Specialty("Eye Care", false),
            Specialty("Cardiology", false),
            Specialty("Pediatric", false),
            Specialty("Neurology", false)
        ))

        val rvDoctors = view.findViewById<RecyclerView>(R.id.rvDoctors)
        rvDoctors.adapter = DoctorAdapter(listOf(
            Doctor("Dr. Sarah Johnson", "Senior Cardiologist", "4.8", "(120 reviews)", true, R.mipmap.ic_launcher),
            Doctor("Dr. James Miller", "General Dentist", "4.9", "(245 reviews)", false, R.mipmap.ic_launcher),
            Doctor("Dr. Elena Rodriguez", "Senior Pediatrician", "4.7", "(89 reviews)", true, R.mipmap.ic_launcher),
            Doctor("Dr. David Chen", "Eye Specialist", "5.0", "(56 reviews)", true, R.mipmap.ic_launcher)
        )) { doctor ->
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, com.example.signspeak.BookAppointmentFragment())
                .addToBackStack(null)
                .commit()
        }

        return view
    }
}
