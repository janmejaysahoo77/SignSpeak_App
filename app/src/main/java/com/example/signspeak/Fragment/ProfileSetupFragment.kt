package com.example.signspeak.Fragment

import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.signspeak.R
import com.example.signspeak.data.ImageUploader
import com.example.signspeak.data.UserProfile
import com.example.signspeak.data.UserRepository
import com.example.signspeak.databinding.FragmentProfileSetupBinding
import kotlinx.coroutines.launch

class ProfileSetupFragment : Fragment() {

    private var _binding: FragmentProfileSetupBinding? = null
    private val binding get() = _binding!!

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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileSetupBinding.inflate(inflater, container, false)

        setupSpinners()
        setupImagePicker()
        setupSaveButton()

        return binding.root
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
            requireContext(),
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

        viewLifecycleOwner.lifecycleScope.launch {
            val imageResult = imageUploader.uploadImage(requireContext(), selectedImageUri!!)

            if (imageResult.isFailure) {
                Toast.makeText(
                    requireContext(),
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
                    requireContext(),
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
            Toast.makeText(requireContext(), "Please enter your name", Toast.LENGTH_SHORT).show()
            binding.etName.requestFocus()
            return false
        }

        if (ageText.isEmpty() || ageText.toIntOrNull() == null || ageText.toInt() <= 0) {
            Toast.makeText(requireContext(), "Please enter a valid age", Toast.LENGTH_SHORT).show()
            binding.etAge.requestFocus()
            return false
        }

        if (emergency1.isEmpty() && emergency2.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter at least one emergency contact", Toast.LENGTH_SHORT).show()
            binding.etEmergency1.requestFocus()
            return false
        }

        if (selectedImageUri == null) {
            Toast.makeText(requireContext(), "Please select a profile image", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    private fun showSuccessOverlay() {
        binding.successOverlay.visibility = View.VISIBLE

        val fadeIn = AnimationUtils.loadAnimation(requireContext(), R.anim.anim_fade_in)
        binding.successOverlay.startAnimation(fadeIn)

        val scaleUp = AnimationUtils.loadAnimation(requireContext(), R.anim.anim_scale_up)
        binding.successCheckIcon.startAnimation(scaleUp)

        val pulseAnim = AnimationUtils.loadAnimation(requireContext(), R.anim.pulse_scale)
        binding.successGlow.startAnimation(pulseAnim)

        Handler(Looper.getMainLooper()).postDelayed({
            if (isAdded) {
                requireContext().getSharedPreferences("signspeak_prefs", android.content.Context.MODE_PRIVATE)
                    .edit().putBoolean("profile_setup_done", true).apply()
                findNavController().navigate(R.id.homeFragment)
            }
        }, 2000)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
