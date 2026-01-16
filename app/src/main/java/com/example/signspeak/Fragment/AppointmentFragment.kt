package com.example.signspeak.Fragment

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.signspeak.DoctorPageActivity
import com.example.signspeak.R

class AppointmentFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout
        val view = inflater.inflate(R.layout.fragment_appointment, container, false)

        // Find the Consult Now button inside fragment XML
        val btnConsultNow = view.findViewById<View>(R.id.btnConsultNow)

        // Navigate to DoctorPageActivity on click
        btnConsultNow.setOnClickListener {
            val intent = Intent(requireContext(), DoctorPageActivity::class.java)
            startActivity(intent)
        }

        return view
    }

    companion object {
        @JvmStatic
        fun newInstance() = AppointmentFragment()
    }
}
