# 🏆 MAD — Sport Event Organizer

**MAD (Mobile App Development)** — Sport Event Organizer is an Android mobile application designed for organizing **amateur sports competitions**. The app helps event managers reduce manual work and paperwork by providing an easy-to-use, mobile-first solution for creating, managing, and tracking sporting events.

**Status:** In active development  
**Platform:** Android  
**Minimum requirements:** JDK 11+, Android SDK, Android Studio (recommended)

---

## What Problem Does This Solve?

Organizing local sports tournaments is often chaotic.
*   **The Problem:** Organizers rely on messy spreadsheets or pen-and-paper brackets. Referees have to physically run scores to a central table. Spectators are left guessing when their friend is playing next.
*   **Our Solution:**
    *   **For Organizers:** Create a tournament in seconds. Define field counts, rules, and let the app auto-generate the schedule (Round Robin or Knockout).
    *   **For Referees:** A secure, simple login to update scores live from the sideline. No more paper scorecards.
    *   **For Teams & Fans:** Real-time access to standings and brackets. Know exactly where and when you play next.

---

## Permissions & Privacy

This app is designed to respect user privacy.

*   **Permissions:**
    *   `INTERNET`: Required to fetch tournament data from the cloud (Supabase) and retrieve weather forecasts.
    *   *No other permissions (Camera, Location, Contacts) are required.*

*   **Data Handling:**
    *   **Authentication:** User passwords are hashed securely before storage.
    *   **Storage:** All tournament data (scores, teams, schedules) is stored securely in a remote Supabase (PostgreSQL) database.
    *   **Tracking:** The app does not track user location or device identifiers.

---

## Table of Contents

- Project Overview  
- Goals  
- Main Features  
- Team  
- Tools & Technologies  
- Installation & Dependencies  
- Build Instructions  
- Usage & Demo  
- Project Structure  
- Wireframes / Mockups  
- Contributing  
- License  

---

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## 📱 Project Overview

The **Sport Event Organizer** is a lightweight Android app aimed at **small-scale and amateur sports events**, such as local tournaments, school competitions, or friendly matches.

The application allows organizers to:
- Create and manage events
- Register teams and participants
- Automatically generate fixtures
- Track live scores and results

The focus is on **simplicity, usability, and extensibility**, making it easy to adapt the app for different sports and event formats.

---

## 🎯 Goals

- Reduce manual scheduling and administrative overhead  
- Provide a clean and intuitive mobile-first interface  
- Enable fast fixture generation and scoreboard updates  
- Keep the codebase modular and easy to extend  

---

## ✨ Main Features

- Create and manage sports events (matches, tournaments)
- Register teams and participants with metadata (age group, category)
- Automatic fixture and schedule generation
- Live scoreboard and results tracking
- View rankings and event history
- Export or print event summaries and results

---

## 👥 Team

- Philip Paškov  
- Heinrich Vannas  
- Oskar Männik  
- Rauno Valo  
- Siim Nigul  

---

## 🛠 Tools & Technologies

- **Language:** Kotlin  
- **Platform:** Android  
- **Build System:** Gradle (Gradle Wrapper included)  
- **IDE:** Android Studio  
- **Version Control:** Git & GitHub  

---

## Technical Highlights

For a deep dive into our database schema, bracket generation algorithms, and system design, please see our [Architecture & Algorithms Documentation](docs/ARCHITECTURE.md).

### Key Features
*   **Recursive Bracket Generation:** Automatically builds tournament trees for any number of teams.
*   **Real-time Standings:** Custom `Comparator` logic sorts league tables by Points > Goal Diff > Goals Scored.
*   **Asynchronous Data:** Uses Kotlin Coroutines for non-blocking database and weather API calls.

---

## ⚙️ Installation & Dependencies

### Requirements

- Java Development Kit (JDK) 11 or newer  
- Android SDK (platforms & build tools for target SDK)  
- Android Studio (recommended) or command-line Gradle  

### Environment setup (macOS / zsh)
```bash
# Install JDK (example using Homebrew)
brew install openjdk@17

# Set JAVA_HOME
echo 'export JAVA_HOME="/usr/local/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home"' >> ~/.zshrc
source ~/.zshrc

# Verify Java installation
java -version
```

🏗 Build Instructions

From the repository root:
```bash
# Clean and assemble debug APK
./gradlew clean assembleDebug

# Run unit tests
./gradlew test

# Run instrumentation tests (requires emulator or device)
./gradlew connectedAndroidTest
```

You can also open the project directly in Android Studio for easier development, previews, and emulator management.

---

## 📸 Usage & Demo

[DEMO](https://drive.google.com/file/d/1euVF-HODnFY74Z3SUfFDM0iNzhpLZMi0/view?resourcekey)

---

## 🗂 Project Structure
```bash
.
├── app/                     # Android application module
│   ├── build.gradle.kts
│   ├── src/
│   │   ├── main/
│   │   │   ├── AndroidManifest.xml
│   │   │   ├── java/         # Kotlin source code
│   │   │   └── res/          # Layouts, drawables, strings
│   └── proguard-rules.pro
├── docs/                    # Documentation, prototypes, screenshots
│   └── paper-prototype/
├── gradle/                  # Gradle configuration
├── build.gradle.kts
├── settings.gradle.kts
└── gradle.properties
```
Build outputs (app/build/) are generated by Gradle and should not be committed.

---

## 🎨 Wireframes / Mockups

Initial UI sketches and mockups were created in Figma:

https://www.figma.com/design/HGYZaQNY97f8Z4YYkAvwpt/Untitled?node-id=0-1


