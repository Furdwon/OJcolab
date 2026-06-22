# OJColab — Google Colab Mobile Companion

OJColab is a native Android application built with Kotlin and Jetpack Compose. It is uniquely designed to run and interact with **Google Colab Notebooks** directly from your phone. 

With a minimalist display footprint, OJColab strips away heavy desktop toolbar elements and adds a custom, touch-optimized floating control panel. Running, stopping, and reloading your notebook execution on a phone is now as simple as a single touch.

---

## Technical Specifications

- **Language:** Kotlin
- **UI Framework:** Jetpack Compose (Material Design 3)
- **Minimum SDK (minSdk):** 26
- **Target SDK (targetSdk):** 35
- **Android Gradle Plugin (AGP):** 8.7.3
- **Kotlin Compiler:** 2.1.0

---

## 🚀 1. How to Open the Project in Android Studio

1. **Download and Install:** Ensure you have the latest stable version of **Android Studio** (Ladybug or newer).
2. **Open Project:**
   - Launch Android Studio.
   - Click **File > Open...** (or choose **Open** from the welcome window).
   - Navigate to the root folder where you extracted the project and select the root directory (containing `build.gradle.kts` and `/app`).
   - Click **OK**.
3. **Gradle Sync:** Allow Android Studio to automatically download dependencies and index files. This can take several minutes. Once complete, click the **Run** button (green play icon next to the configuration selector) to build and run the app.

---

## 🔑 2. How to Get SHA-1 & Configure Google Cloud Console

Google Colab requires a Google Account login. In order for Google Sign-In to succeed inside the Chrome Custom Tabs interface without security warnings:

### Step A: Generate Your SHA-1 Key
Depending on your platform, run the following command in terminal/command prompt to retrieve your local developer certificate fingerprint:

* **macOS / Linux:**
  ```bash
  keytool -list -v -alias androiddebugkey -keystore ~/.android/debug.keystore -storepass android
  ```
* **Windows:**
  ```cmd
  keytool -list -v -alias androiddebugkey -keystore %USERPROFILE%\.android\debug.keystore -storepass android
  ```

Copy the hexadecimal sequence next to **SHA1** (e.g., `45:3A:BB:2E:...`).

### Step B: Configure in Google Cloud Console
1. Go to the [Google Cloud Console](https://console.cloud.google.com/).
2. Select your Google Developer project (or create a new one).
3. Navigate to **APIs & Services > Credentials**.
4. Click **+ Create Credentials** and select **OAuth client ID**.
5. Choose **Android** as the Application Type.
6. Fill in the fields:
   - **Name:** OJColab Debug Key
   - **Package name:** `com.ojcolab.app` (matches your `applicationId`)
   - **SHA-1 certificate fingerprint:** Paste the SHA-1 copied from Step A.
7. Click **Create** to register the app config.

---

## 🔗 3. How to Find Your Colab Notebook URL

1. Open standard Google Colab on your computer: [Google Colab](https://colab.research.google.com/).
2. Create or navigate to any existing python notebook (.ipynb) in your Google Drive.
3. Locate the address bar at the top of your web browser.
4. Copy the complete URL. It will look like this:
   `https://colab.research.google.com/drive/1A_bCdEfGhIjKlMnOpQrStUvWxYz1234`
5. Send or email that copy to your phone so you can easily paste it.

---

## 📱 4. How to Use the App

OJColab is exceptionally streamlined with 2 key screens:

### Screen 1: Notebook Configuration (First Launch)
- When you launch the app, you are greeted with the **Notebook Configuration** screen.
- **URL Input:** Paste your copied Google Colab URL in the "Colab Notebook URL" input container.
- **Save Action:** Tap **Save & Open Notebook**. The URL is saved in secure local preferences (`SharedPreferences`) on your phone, so you will launch directly into the Viewer next time.

### Screen 2: Notebook Viewer
The main screen automatically renders the Google Colab Workspace using a desktop agent. The standard sidebar, menu, and header bars are automatically stripped away to give you a full-screen view of the code and execution results.

- **Floating Control Panel (Bottom Center):**
  - **[⚙] Settings Icon:** Instantly takes you back to the Settings configuration screen to modify or change URLs.
  - **[▶ Run All] (Green):** One-tap execution of the entire notebook. This triggers Colab's menu trigger and falls back to dispatcher keyboard command simulations.
  - **[⬛ Stop] (Red):** Interrupts currently running cell executions instantly.
  - **[↺ Reload] (Grey):** Force reloads the browser environment to clear runtime execution logs or test clean loads.

- **Automatic Google Authentication Redirect:**
  - Google Colab uses secure sign-in cookies. If you click "Sign In", OJColab launches Chrome Custom Tabs in dark-mode style.
  - Login safely directly with Google's official OAuth flow.
  - Closing the login custom tab automatically returns you to OJColab and reloads your workspace session seamlessly!
