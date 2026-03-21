package com.example.signspeak

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import android.widget.LinearLayout
import com.example.signspeak.Fragment.DoctorsFragment

class MedicalActivity : AppCompatActivity() {

    private lateinit var tabHome: LinearLayout
    private lateinit var tabAppointments: LinearLayout
    private lateinit var tabPrescription: LinearLayout
    private lateinit var tabPharmacy: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_medical)

        tabHome = findViewById(R.id.tabHome)
        tabAppointments = findViewById(R.id.tabAppointments)
        tabPrescription = findViewById(R.id.tabPrescription)
        tabPharmacy = findViewById(R.id.tabPharmacy)

        // Load default fragment
        if (savedInstanceState == null) {
            loadFragment(DoctorsFragment())
            updateTabs(R.id.tabHome)
        }

        tabHome.setOnClickListener {
            loadFragment(DoctorsFragment())
            updateTabs(R.id.tabHome)
        }
        tabAppointments.setOnClickListener {
            loadFragment(MedicalAppointmentFragment())
            updateTabs(R.id.tabAppointments)
        }
        tabPrescription.setOnClickListener {
            loadFragment(MedicalPrescriptionFragment())
            updateTabs(R.id.tabPrescription)
        }
        tabPharmacy.setOnClickListener {
            loadFragment(MedicalPharmacyFragment())
            updateTabs(R.id.tabPharmacy)
        }
    }

    private fun updateTabs(selectedId: Int) {
        tabHome.isSelected = (tabHome.id == selectedId)
        tabAppointments.isSelected = (tabAppointments.id == selectedId)
        tabPrescription.isSelected = (tabPrescription.id == selectedId)
        tabPharmacy.isSelected = (tabPharmacy.id == selectedId)
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}
