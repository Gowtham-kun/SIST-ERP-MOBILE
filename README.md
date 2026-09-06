# 📱 SIST ERP Mobile Application

> High-performance, offline-first Android application for Sathyabama Institute of Science and Technology (SIST) student ERP portal.

---

## 🚀 Key Highlights & Mobile Architecture

- ⚡ **Zero-Wait Offline Launch (Instant Boot)**:
  Once authenticated, your profile, attendance summary, daily logs, CAE exam results, and timetable schedule are permanently stored in local encrypted cache. Opening the app immediately paints the dashboard in under 50ms without loading spinners or blank screens, even with zero internet.
- 🔄 **Silent Background Revalidation (Stale-While-Revalidate)**:
  When opened with an active internet connection, fresh attendance records and timetable schedules are silently synchronized in the background without interrupting your view or resetting scroll positions.
- 🏎️ **Buttery-Smooth 120Hz Display Optimization**:
  Engineered specifically for high refresh rate Android devices (90Hz, 120Hz, 144Hz). Configured with hardware acceleration, passive touch event handling, and GPU-composited obsidian dark theme.
- 🌐 **Direct Standalone Native Bridge**:
  Powered by native Android `AndroidBridge`, executing direct HTTPS requests with official ERP origin headers. The app runs completely standalone on your phone—no PC or local Node.js server required.
- 📦 **Downloadable APK**:
  Ready-to-install Android APK packaged directly in this repository.

---

## 📲 Download & Install APK

You can download the APK file directly from this repository:

👉 **[Download SIST-ERP.apk](SIST-ERP.apk)**

### Installation Steps:
1. Download `SIST-ERP.apk` to your Android device.
2. Tap the downloaded file in your notification bar or Files app.
3. If prompted, allow *"Install unknown apps"* for your browser/file manager.
4. Tap **Install** and open **SIST ERP**.
5. Log in once with your Register Number and Password. You will stay logged in permanently!

---

## 🛠️ Project Structure

```
SIST-ERP-MOBILE/
├── android/                   # Native Android Studio / Gradle project
│   ├── app/
│   │   ├── src/main/
│   │   │   ├── AndroidManifest.xml
│   │   │   ├── assets/        # Bundled web assets (offline-ready)
│   │   │   │   ├── index.html
│   │   │   │   ├── app.js
│   │   │   │   ├── portal-api.js
│   │   │   │   ├── styles.css
│   │   │   │   └── anime.esm.js
│   │   │   └── java/com/sist/erp/
│   │   │       └── MainActivity.java # 120Hz mode & AndroidBridge
│   │   └── build.gradle.kts
│   ├── build.gradle.kts
│   └── gradlew / gradlew.bat
├── .github/workflows/
│   └── build-apk.yml          # Automated CI/CD APK builder on push
├── SIST-ERP.apk               # Pre-compiled, ready-to-install Android APK
├── app.js                     # Offline-first client application controller
├── portal-api.js              # Direct ERP client & dual-mode auth manager
├── styles.css                 # 120Hz hardware-accelerated Obsidian styling
├── index.html                 # Mobile-first responsive markup
└── server.js                  # Node.js REST server (desktop/web dev fallback)
```

---

## 💻 Building from Source

To build the APK locally:

```bash
cd android
./gradlew assembleDebug
```

The output APK will be generated at `android/app/build/outputs/apk/debug/app-debug.apk`.
