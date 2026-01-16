# 🧏‍♂️ SignSpeak — Assistive Communication App

SignSpeak is an assistive communication Android application designed for deaf and mute individuals.  
It helps users communicate more naturally using **Gesture → Speech**, **Speech → Gesture**, and **Sign Language Learning**, along with emergency SOS features.

---

## 🚀 Features

### 🎤 Speech → Gesture
Convert spoken voice to sign-language gestures/animations.

### ✋ Gesture → Speech
Detect hand gestures and convert them into spoken voice using TTS.

### 📚 Learn Sign Language
Interactive learning module for learning sign gestures.

### 🆘 SOS Emergency Button
- Plays emergency alarm tone
- Vibrates device
- Opens dialer with configured emergency number

### 🔔 Alerts & Notification Bar
Displays critical system/info alerts (e.g., medical or family messages)

### 🌐 Firebase Authentication
- Email/Password login & signup
- Profile name synced with Firebase Auth

### 🔒 Secure Storage
No user-sensitive data stored locally.

### 🌍 Multilingual Support
- English (default)
- Odia
- User-selectable from in-app **Settings**

---

## 🛠 Tech Stack

| Component | Technology |
|---|---|
| Language | Kotlin |
| UI | XML + Material Components |
| Navigation | Jetpack Navigation |
| Auth | Firebase Authentication |
| Database | Firebase Firestore (Upcoming) |
| Notifications | Firebase Cloud Messaging (Upcoming) |
| Audio | MediaPlayer API |
| Gesture | MediaPipe (Upcoming) |
| Speech | Android Speech Recognizer + TTS |
| SOS System | Vibrator + Dialer Intent |

---

## 📁 Project Structure

app/
├─ java/com.example.signspeak/
│ ├─ MainActivity.kt
│ ├─ SettingActivity.kt
│ ├─ fragments/
│ │ ├─ HomeFragment.kt
│ │ ├─ SpeechToGestureFragment.kt
│ │ ├─ GestureToSpeechFragment.kt
│ │ ├─ LearnSignFragment.kt
│ └─ auth/
│ ├─ LoginActivity.kt
│ ├─ SignupActivity.kt
│
└─ res/
├─ layout/
├─ values/
├─ values-or/
├─ navigation/
└─ drawable/

yaml
Copy code

---

## 📦 Prerequisites

Before running:

✔ Android Studio Flamingo or newer  
✔ Android SDK 24+  
✔ Java JDK 17+  
✔ Firebase Project created  
✔ Active internet connection

---

## 🔧 Firebase Setup

1. Go to **Firebase Console**
2. Create project → Add Android App
3. Add package name (e.g. `com.example.signspeak`)
4. Download `google-services.json`
5. Place it in:

app/src/google-services.json

markdown
Copy code

6. Enable:
✔ Authentication → Email/Password  
✔ Firestore Database (Optional for now)

7. Add dependencies in `build.gradle`:

implementation(platform("com.google.firebase:firebase-bom:33.1.1"))
implementation("com.google.firebase:firebase-auth")
implementation("com.google.firebase:firebase-firestore")

yaml
Copy code

8. Sync project
