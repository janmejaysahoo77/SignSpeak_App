package com.example.signspeak

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.signspeak.data.ImageUploader
import com.example.signspeak.data.UserRepository
import com.google.android.material.button.MaterialButton
import com.google.android.material.switchmaterial.SwitchMaterial
import kotlinx.coroutines.launch

class UserProfileViewActivity : AppCompatActivity() {

    private val userRepository = UserRepository()
    private val imageUploader = ImageUploader()

    private var isEditMode = false
    private var selectedImageUri: Uri? = null
    private var currentImageUrl: String = ""
    private var isSaving = false

    private lateinit var pvName: EditText
    private lateinit var pvAge: EditText
    private lateinit var pvGender: Spinner
    private lateinit var pvDisability: Spinner
    private lateinit var pvSeverity: Spinner
    private lateinit var pvLanguage: Spinner
    private lateinit var pvMode: Spinner
    private lateinit var pvBloodGroup: EditText
    private lateinit var pvCondition: EditText
    private lateinit var pvEmergency1: EditText
    private lateinit var pvEmergency2: EditText
    private lateinit var pvGuardian: EditText
    private lateinit var pvSignUsage: Spinner
    private lateinit var pvExperience: Spinner
    private lateinit var pvSwitchSOS: SwitchMaterial
    private lateinit var profileViewImage: ImageView
    private lateinit var btnChangePhoto: ImageView
    private lateinit var btnSaveProfile: MaterialButton
    private lateinit var btnEditToggle: ImageButton
    private lateinit var profileViewName: TextView
    private lateinit var profileViewEmail: TextView

    private val genderItems = arrayOf("Select Gender", "Male", "Female", "Non-Binary", "Prefer not to say")
    private val disabilityItems = arrayOf("Select Disability", "Deaf", "Hard of Hearing", "Mute", "Deaf-Mute", "None", "Other")
    private val severityItems = arrayOf("Select Severity", "Mild", "Moderate", "Severe", "Profound")
    private val languageItems = arrayOf("Select Language", "English", "Hindi", "Odia", "Bengali", "Tamil", "Telugu", "Marathi", "Other")
    private val modeItems = arrayOf("Select Mode", "Sign Language", "Text", "Speech", "Mixed")
    private val signUsageItems = arrayOf("Select Usage", "Daily", "Frequently", "Sometimes", "Rarely", "Never")
    private val experienceItems = arrayOf("Select Experience", "Beginner", "Intermediate", "Advanced", "Expert")

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            profileViewImage.setImageURI(it)
            profileViewImage.setPadding(0, 0, 0, 0)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile_view)

        initViews()
        setupSpinners()
        setupClickListeners()
        loadProfile()
    }

    private fun initViews() {
        pvName = findViewById(R.id.pvName)
        pvAge = findViewById(R.id.pvAge)
        pvGender = findViewById(R.id.pvGender)
        pvDisability = findViewById(R.id.pvDisability)
        pvSeverity = findViewById(R.id.pvSeverity)
        pvLanguage = findViewById(R.id.pvLanguage)
        pvMode = findViewById(R.id.pvMode)
        pvBloodGroup = findViewById(R.id.pvBloodGroup)
        pvCondition = findViewById(R.id.pvCondition)
        pvEmergency1 = findViewById(R.id.pvEmergency1)
        pvEmergency2 = findViewById(R.id.pvEmergency2)
        pvGuardian = findViewById(R.id.pvGuardian)
        pvSignUsage = findViewById(R.id.pvSignUsage)
        pvExperience = findViewById(R.id.pvExperience)
        pvSwitchSOS = findViewById(R.id.pvSwitchSOS)
        profileViewImage = findViewById(R.id.profileViewImage)
        btnChangePhoto = findViewById(R.id.btnChangePhoto)
        btnSaveProfile = findViewById(R.id.btnSaveProfile)
        btnEditToggle = findViewById(R.id.btnEditToggle)
        profileViewName = findViewById(R.id.profileViewName)
        profileViewEmail = findViewById(R.id.profileViewEmail)
    }

    private fun setupSpinners() {
        setupSpinner(pvGender, genderItems)
        setupSpinner(pvDisability, disabilityItems)
        setupSpinner(pvSeverity, severityItems)
        setupSpinner(pvLanguage, languageItems)
        setupSpinner(pvMode, modeItems)
        setupSpinner(pvSignUsage, signUsageItems)
        setupSpinner(pvExperience, experienceItems)
    }

    private fun setupSpinner(spinner: Spinner, items: Array<String>) {
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

    private fun setupClickListeners() {
        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }

        btnEditToggle.setOnClickListener {
            isEditMode = !isEditMode
            toggleEditMode(isEditMode)
        }

        btnChangePhoto.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }

        btnSaveProfile.setOnClickListener {
            if (!isSaving) {
                updateProfile()
            }
        }
    }

    private fun toggleEditMode(enabled: Boolean) {
        pvName.isEnabled = enabled
        pvAge.isEnabled = enabled
        pvGender.isEnabled = enabled
        pvDisability.isEnabled = enabled
        pvSeverity.isEnabled = enabled
        pvLanguage.isEnabled = enabled
        pvMode.isEnabled = enabled
        pvBloodGroup.isEnabled = enabled
        pvCondition.isEnabled = enabled
        pvEmergency1.isEnabled = enabled
        pvEmergency2.isEnabled = enabled
        pvGuardian.isEnabled = enabled
        pvSignUsage.isEnabled = enabled
        pvExperience.isEnabled = enabled
        pvSwitchSOS.isEnabled = enabled

        btnChangePhoto.visibility = if (enabled) View.VISIBLE else View.GONE
        btnSaveProfile.visibility = if (enabled) View.VISIBLE else View.GONE

        if (enabled) {
            btnEditToggle.setColorFilter(resources.getColor(R.color.colorError, null))
        } else {
            btnEditToggle.setColorFilter(android.graphics.Color.parseColor("#00E5FF"))
        }
    }

    private fun loadProfile() {
        profileViewEmail.text = userRepository.getCurrentUserEmail() ?: ""

        lifecycleScope.launch {
            val result = userRepository.getUserProfile()

            if (result.isSuccess) {
                val data = result.getOrThrow()
                populateFields(data)
            } else {
                Toast.makeText(
                    this@UserProfileViewActivity,
                    "Failed to load profile: ${result.exceptionOrNull()?.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun populateFields(data: Map<String, Any>) {
        pvName.setText(data["name"]?.toString() ?: "")
        pvAge.setText(data["age"]?.toString() ?: "")
        pvBloodGroup.setText(data["bloodGroup"]?.toString() ?: "")
        pvCondition.setText(data["medicalCondition"]?.toString() ?: "")
        pvEmergency1.setText(data["emergency1"]?.toString() ?: "")
        pvEmergency2.setText(data["emergency2"]?.toString() ?: "")
        pvGuardian.setText(data["guardian"]?.toString() ?: "")

        profileViewName.text = data["name"]?.toString() ?: "User"

        setSpinnerSelection(pvGender, genderItems, data["gender"]?.toString())
        setSpinnerSelection(pvDisability, disabilityItems, data["disability"]?.toString())
        setSpinnerSelection(pvSeverity, severityItems, data["severity"]?.toString())
        setSpinnerSelection(pvLanguage, languageItems, data["language"]?.toString())
        setSpinnerSelection(pvMode, modeItems, data["communicationMode"]?.toString())
        setSpinnerSelection(pvSignUsage, signUsageItems, data["signUsage"]?.toString())
        setSpinnerSelection(pvExperience, experienceItems, data["experience"]?.toString())

        pvSwitchSOS.isChecked = data["sosEnabled"] as? Boolean ?: true

        val imageUrl = data["profileImageUrl"]?.toString()
        if (!imageUrl.isNullOrEmpty()) {
            currentImageUrl = imageUrl
            Glide.with(this)
                .load(imageUrl)
                .circleCrop()
                .placeholder(R.drawable.ic_person_placeholder)
                .into(profileViewImage)
            profileViewImage.setPadding(0, 0, 0, 0)
        }
    }

    private fun setSpinnerSelection(spinner: Spinner, items: Array<String>, value: String?) {
        if (value == null) return
        val index = items.indexOf(value)
        if (index >= 0) {
            spinner.setSelection(index)
        }
    }

    private fun updateProfile() {
        val name = pvName.text.toString().trim()
        val ageText = pvAge.text.toString().trim()

        if (name.isEmpty()) {
            Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }
        if (ageText.isEmpty() || ageText.toIntOrNull() == null || ageText.toInt() <= 0) {
            Toast.makeText(this, "Please enter a valid age", Toast.LENGTH_SHORT).show()
            return
        }

        isSaving = true
        btnSaveProfile.isEnabled = false
        btnSaveProfile.text = "Updating..."

        lifecycleScope.launch {
            var imageUrl = currentImageUrl

            if (selectedImageUri != null) {
                val uploadResult = imageUploader.uploadImage(
                    this@UserProfileViewActivity,
                    selectedImageUri!!
                )
                if (uploadResult.isFailure) {
                    Toast.makeText(
                        this@UserProfileViewActivity,
                        "Image upload failed: ${uploadResult.exceptionOrNull()?.message}",
                        Toast.LENGTH_LONG
                    ).show()
                    resetSaveButton()
                    return@launch
                }
                imageUrl = uploadResult.getOrThrow()
            }

            val updates = mapOf<String, Any>(
                "name" to name,
                "age" to ageText.toInt(),
                "gender" to pvGender.selectedItem.toString(),
                "disability" to pvDisability.selectedItem.toString(),
                "severity" to pvSeverity.selectedItem.toString(),
                "language" to pvLanguage.selectedItem.toString(),
                "communicationMode" to pvMode.selectedItem.toString(),
                "bloodGroup" to pvBloodGroup.text.toString().trim(),
                "medicalCondition" to pvCondition.text.toString().trim(),
                "emergency1" to pvEmergency1.text.toString().trim(),
                "emergency2" to pvEmergency2.text.toString().trim(),
                "guardian" to pvGuardian.text.toString().trim(),
                "signUsage" to pvSignUsage.selectedItem.toString(),
                "experience" to pvExperience.selectedItem.toString(),
                "sosEnabled" to pvSwitchSOS.isChecked,
                "profileImageUrl" to imageUrl
            )

            val saveResult = userRepository.updateUserProfile(updates)

            if (saveResult.isSuccess) {
                currentImageUrl = imageUrl
                selectedImageUri = null
                profileViewName.text = name

                Toast.makeText(
                    this@UserProfileViewActivity,
                    "Profile updated successfully!",
                    Toast.LENGTH_SHORT
                ).show()

                isEditMode = false
                toggleEditMode(false)
            } else {
                Toast.makeText(
                    this@UserProfileViewActivity,
                    "Failed to update: ${saveResult.exceptionOrNull()?.message}",
                    Toast.LENGTH_LONG
                ).show()
            }

            resetSaveButton()
        }
    }

    private fun resetSaveButton() {
        isSaving = false
        btnSaveProfile.isEnabled = true
        btnSaveProfile.text = "UPDATE PROFILE"
    }
}
