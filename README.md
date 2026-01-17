🧏‍♂️ SignSpeak Platform — Accessible Communication Ecosystem

SignSpeak is a cross-platform accessibility initiative designed to assist deaf and mute individuals by enabling seamless communication through gesture recognition, speech synthesis, and sign language technologies.

The ecosystem currently includes:

✔ Android App — Assistive Communication
✔ Doctor Telemedicine Website — Accessible Healthcare
✔ Business/API Marketplace Website — SignSpeak+ APIs

1️⃣ SignSpeak Android App — Assisted Communication

A communication app allowing users to convert between Gesture ↔ Speech, learn sign language, and send SOS emergency alerts.

🚀 Core Features

🎤 Speech → Gesture

✋ Gesture → Speech (TTS)

📚 Learn Sign Language

🆘 SOS Emergency

🌍 English + Odia Language

🔐 Firebase Auth

🔔 Alerts & Notifications

🎯 High Accessibility UI

📦 Tech Stack
Area	Tech
Language	Kotlin
UI	XML + Material Components
Auth	Firebase Auth
Data	Firestore (Upcoming)
Gesture	MediaPipe
Speech	ASR + TTS
SOS	Vibrator + Dialer Intent
Build	Gradle
🛠 Run Instructions (Android)
1. Open in Android Studio
2. Add google-services.json
3. Sync Gradle
4. Connect phone/emulator
5. Run


Firebase required settings:
✔ Enable Email/Password Auth
✔ Add Firestore (optional)

2️⃣ Doctor Telemedicine Website — Accessible Healthcare

A WebRTC-based real-time teleconsultation system enabling remote doctor-patient video consultations with accessibility support for deaf/mute users.

🎯 Purpose

Reduce hospital visits

Provide remote consultations

Support accessibility modalities

Enable doctor dashboard workflows

🚀 Features
👨‍⚕️ Doctor Portal (UI)

Login/Signup UI

Patient & appointment info

Video call initiation

Session stats

📹 Video Call Room

WebRTC real-time communication

Camera toggle

Mic toggle

End call

Clean accessible UI

🧏 Accessibility Enhancements

Caption placeholder overlay

Sign interpretation area (future)

High contrast UI design

🔐 Session Handling

Session joins are channel-based

Required Patient Channel:
doctorchannel

Doctor shares the same channel with patient.

🛠 Tech Stack
Layer	Technology
Frontend	HTML + CSS + JavaScript
Video	Agora WebRTC SDK
Auth	UI-based (demo)
State	LocalStorage
Backend	None (prototype)
📁 Project Structure
doctor-video-call/
├── DoctorPortal.html
├── index.html
└── README.md

⚙️ Setup & Run
git clone https://github.com/janmejaysahoo77/SignSpeak_App.git
cd SignSpeak_App/doctor-video-call
Open DoctorPortal.html in browser

Agora Config

Inside index.html:

const AGORA_APP_ID = "YOUR_AGORA_APP_ID";
const AGORA_TEMP_TOKEN = null;


Token can be null for development.

🧪 Current Status

✔ UI complete
✔ Dashboard functional
✔ Video call working


🔮 Future Roadmap

Speech-to-text captions

Sign language recognition overlay

Encrypted token-based sessions

Medical PDF sharing

Integration with Android app

3️⃣ SignSpeak+ — API Marketplace & Business Website

A polished SaaS-style website for showcasing SignSpeak APIs and platform information.

🚀 Core Features

✔ 7 Complete Pages:
Home / Products / Use Cases / Pricing / Dev Portal / About / Contact

✔ Modern responsive UI
✔ Tailwind + Vite + React 18
✔ Framer Motion animations
✔ Component architecture
✔ Enterprise-facing branding

📦 Tech Stack
Category	Tech
Framework	React 18
Build Tool	Vite
Styling	Tailwind CSS
Animations	Framer Motion
Routing	React Router
🧱 Project Structure
src/
├── components/
├── pages/
├── App.jsx
├── main.jsx
└── index.css

🛠 Setup & Run (Business Site)
npm install
npm run dev


Production:

npm run build
npm run preview

🌟 APIs Showcased

Gesture → Speech API

Speech → Gesture API

Emotion Detection API

Real-Time Translation API

Video Call Accessibility API

🎯 Use Cases

Healthcare

Government

OTT & Media

Education

Smart Cities

🧩 Ecosystem Summary
Component	Platform	Status
SignSpeak App	Android	✔ Active
Doctor Telemedicine	Web	✔ Prototype
SignSpeak+ Marketplace	Web	✔ Business UI
Emotion Recognition	ML	⏳ Done
Translation APIs	SaaS	⏳ Planned
📍 Running Everything Together
Module	Platform	Run Instructions
Android App	Android Studio	Run
Doctor Website	Browser	Open HTML
API Marketplace	VS Code	npm run dev

Channel required for Telemedicine:

doctorchannel

🧑‍💻 Team Credits

Built by:

Team Technovators 🚀



