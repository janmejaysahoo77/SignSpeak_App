package com.example.signspeak

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.signspeak.data.ImageUploader
import com.example.signspeak.data.UserProfile
import com.example.signspeak.data.UserRepository
import com.example.signspeak.databinding.FragmentProfileSetupBinding
import kotlinx.coroutines.launch

class ProfileSetupActivity : AppCompatActivity() {

    private lateinit var binding: FragmentProfileSetupBinding

    private var selectedImageUri: Uri? = null
    private val imageUploader = ImageUploader()
    private val userRepository = UserRepository()
    private var isSaving = false

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            binding.imgProfile.setImageURI(it)
            binding.imgProfile.setPadding(0, 0, 0, 0)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentProfileSetupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupSpinners()
        setupImagePicker()
        setupSaveButton()
    }

    private fun setupImagePicker() {
        binding.imgProfile.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }
    }

    private fun setupSpinners() {
        setupSpinner(binding.spGender, arrayOf("Select Gender", "Male", "Female", "Non-Binary", "Prefer not to say"))
        setupSpinner(binding.spDisability, arrayOf("Select Disability", "Deaf", "Hard of Hearing", "Mute", "Deaf-Mute", "None", "Other"))
        setupSpinner(binding.spSeverity, arrayOf("Select Severity", "Mild", "Moderate", "Severe", "Profound"))
        setupSpinner(binding.spLanguage, arrayOf("Select Language", "English", "Hindi", "Odia", "Bengali", "Tamil", "Telugu", "Marathi", "Other"))
        setupSpinner(binding.spMode, arrayOf("Select Mode", "Sign Language", "Text", "Speech", "Mixed"))
        setupSpinner(binding.spSignUsage, arrayOf("Select Usage", "Daily", "Frequently", "Sometimes", "Rarely", "Never"))
        setupSpinner(binding.spExperience, arrayOf("Select Experience", "Beginner", "Intermediate", "Advanced", "Expert"))
    }

    private fun setupSpinner(spinner: android.widget.Spinner, items: Array<String>) {
        val adapter = object : ArrayAdapter<String>(
            this,
            android.R.layout.simple_spinner_item,
            items
        ) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent)
                (view as TextView).apply {
                    setTextColor(resources.getColor(R.color.colorOnSurface, null))
                    textSize = 14f
                }
                return view
            }

            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getDropDownView(position, convertView, parent)
                (view as TextView).apply {
                    setTextColor(resources.getColor(R.color.colorOnSurface, null))
                    setBackgroundColor(resources.getColor(R.color.colorSurface, null))
                    setPadding(32, 24, 32, 24)
                    textSize = 14f
                }
                return view
            }
        }
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
    }

    private fun setupSaveButton() {
        binding.btnSave.setOnClickListener { view ->
            view.animate()
                .scaleX(0.95f)
                .scaleY(0.95f)
                .setDuration(100)
                .withEndAction {
                    view.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(100)
                        .start()
                }
                .start()

            if (!isSaving && validateInputs()) {
                saveProfile()
            }
        }
    }

    private fun saveProfile() {
        isSaving = true
        binding.btnSave.isEnabled = false
        binding.btnSave.text = "Saving..."

        lifecycleScope.launch {
            val imageResult = imageUploader.uploadImage(this@ProfileSetupActivity, selectedImageUri!!)

            if (imageResult.isFailure) {
                Toast.makeText(
                    this@ProfileSetupActivity,
                    "Image upload failed: ${imageResult.exceptionOrNull()?.message}",
                    Toast.LENGTH_LONG
                ).show()
                resetSaveButton()
                return@launch
            }

            val imageUrl = imageResult.getOrThrow()

            val userProfile = UserProfile(
                name = binding.etName.text.toString().trim(),
                age = binding.etAge.text.toString().trim().toInt(),
                gender = binding.spGender.selectedItem.toString(),
                disability = binding.spDisability.selectedItem.toString(),
                severity = binding.spSeverity.selectedItem.toString(),
                language = binding.spLanguage.selectedItem.toString(),
                communicationMode = binding.spMode.selectedItem.toString(),
                bloodGroup = binding.etBloodGroup.text.toString().trim(),
                medicalCondition = binding.etCondition.text.toString().trim(),
                emergency1 = binding.etEmergency1.text.toString().trim(),
                emergency2 = binding.etEmergency2.text.toString().trim(),
                guardian = binding.etGuardian.text.toString().trim(),
                signUsage = binding.spSignUsage.selectedItem.toString(),
                experience = binding.spExperience.selectedItem.toString(),
                sosEnabled = binding.switchSOS.isChecked,
                profileImageUrl = imageUrl
            )

            val saveResult = userRepository.saveUserProfile(userProfile)

            if (saveResult.isFailure) {
                Toast.makeText(
                    this@ProfileSetupActivity,
                    "Failed to save profile: ${saveResult.exceptionOrNull()?.message}. Please try again.",
                    Toast.LENGTH_LONG
                ).show()
                resetSaveButton()
                return@launch
            }

            showSuccessOverlay()
        }
    }

    private fun resetSaveButton() {
        isSaving = false
        binding.btnSave.isEnabled = true
        binding.btnSave.text = "SAVE PROFILE"
    }

    private fun validateInputs(): Boolean {
        val name = binding.etName.text.toString().trim()
        val ageText = binding.etAge.text.toString().trim()
        val emergency1 = binding.etEmergency1.text.toString().trim()
        val emergency2 = binding.etEmergency2.text.toString().trim()

        if (name.isEmpty()) {
            Toast.makeText(this, "Please enter your name", Toast.LENGTH_SHORT).show()
            binding.etName.requestFocus()
            return false
        }

        if (ageText.isEmpty() || ageText.toIntOrNull() == null || ageText.toInt() <= 0) {
            Toast.makeText(this, "Please enter a valid age", Toast.LENGTH_SHORT).show()
            binding.etAge.requestFocus()
            return false
        }

        if (emergency1.isEmpty() && emergency2.isEmpty()) {
            Toast.makeText(this, "Please enter at least one emergency contact", Toast.LENGTH_SHORT).show()
            binding.etEmergency1.requestFocus()
            return false
        }

        if (selectedImageUri == null) {
            Toast.makeText(this, "Please select a profile image", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    private fun showSuccessOverlay() {
        binding.successOverlay.visibility = View.VISIBLE

        val fadeIn = AnimationUtils.loadAnimation(this, R.anim.anim_fade_in)
        binding.successOverlay.startAnimation(fadeIn)

        val scaleUp = AnimationUtils.loadAnimation(this, R.anim.anim_scale_up)
        binding.successCheckIcon.startAnimation(scaleUp)

        val pulseAnim = AnimationUtils.loadAnimation(this, R.anim.pulse_scale)
        binding.successGlow.startAnimation(pulseAnim)

        Handler(Looper.getMainLooper()).postDelayed({
            getSharedPreferences("signspeak_prefs", Context.MODE_PRIVATE)
                .edit().putBoolean("profile_setup_done", true).apply()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }, 2000)
    }
}
