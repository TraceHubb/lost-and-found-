# Firebase setup + run guide (LostAndFoundApp)

This project uses:

- **Firebase Authentication** (Email/Password)
- **Cloud Firestore** (to store lost/found posts)
- **Firebase Storage** (to store item images)

Your Android package / applicationId is:

- `com.lostandfound`

---

## 1) Create Firebase project

1. Go to Firebase Console: **Firebase** → **Add project**
2. Create the project (any name is fine).

---

## 2) Register the Android app in Firebase

1. In Firebase Console → **Project settings** → **Your apps** → **Add app** → **Android**
2. **Android package name**: `com.lostandfound`
3. (Optional) **App nickname**: anything
4. (Optional) **Debug SHA-1**: *not required* for Email/Password auth.  
   You only need SHA-1 for things like Google Sign-In / Phone Auth, etc.
5. Click **Register app**

---

## 3) Download and add `google-services.json`

1. Download `google-services.json` from Firebase Console.
2. Place it here (IMPORTANT path):

`LostAndFoundApp/app/google-services.json`

If you put it in the wrong folder, Firebase will not initialize correctly.

---

## 4) Enable Authentication (Email/Password)

1. Firebase Console → **Build** → **Authentication**
2. **Get started**
3. **Sign-in method**
4. Enable **Email/Password**

---

## 5) Create Firestore database

1. Firebase Console → **Build** → **Firestore Database**
2. Click **Create database**
3. Choose a location close to you
4. For development, you can start in **test mode**.

### Firestore data model used by this app

Collection:

- `items`

Each document stores fields like:

- `itemName` (String)
- `description` (String)
- `location` (String)
- `date` (Number / Long)
- `imageUrl` (String)
- `type` (String: `"LOST"` or `"FOUND"`)
- `status` (String: `"ACTIVE"`, `"RECOVERED"`, `"CLAIMED"`)
- `userId` (String)
- `datePosted` (Number / Long)
- `contactEmail` (String)
- `contactPhone` (String)

---

## 6) Enable Firebase Storage

1. Firebase Console → **Build** → **Storage**
2. Click **Get started**
3. For development, you can start in **test mode**.

This app uploads images to:

- `items/<docId>.jpg`

---

## 7) (Recommended) Security rules

### Development-only rules (easy testing)

Use these temporarily while building (do **not** keep them for production).

#### Firestore rules (development)

```txt
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /{document=**} {
      allow read, write: if request.auth != null;
    }
  }
}
```

#### Storage rules (development)

```txt
rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {
    match /{allPaths=**} {
      allow read, write: if request.auth != null;
    }
  }
}
```

Meaning:

- Users must be **logged in** to read/write.

---

## 8) Run the app in Android Studio

### A) Open project

1. Open **Android Studio**
2. **File → Open**
3. Select the folder:

`LostAndFoundApp`

4. Wait for **Gradle sync** to finish

### B) Create / select emulator or device

1. **Tools → Device Manager**
2. Create a virtual device (or connect a phone with USB debugging)

### C) Run

1. Click the green **Run** button

---

## 9) IMPORTANT: Your project is missing the Gradle wrapper scripts

Right now your `LostAndFoundApp` folder is missing:

- `gradlew`
- `gradlew.bat`
- `gradle/wrapper/gradle-wrapper.jar`

Without these, running from the command line is harder and some environments fail to sync.

### Fix option 1 (recommended): Regenerate wrapper using a local Gradle install

If you have Gradle installed on your PC:

1. Open **PowerShell** in `LostAndFoundApp`
2. Run:

```powershell
gradle wrapper
```

This will recreate the wrapper scripts and jar.

### Fix option 2: Copy wrapper files from a new Android Studio project

1. In Android Studio: **New Project** (Empty Compose Activity)
2. Make sure it uses the same Gradle/AGP family (recent versions)
3. Copy these files/folders from the new project into this one:
   - `gradlew`
   - `gradlew.bat`
   - `gradle/wrapper/gradle-wrapper.jar`
   - `gradle/wrapper/gradle-wrapper.properties`

Then re-open `LostAndFoundApp` and sync again.

---

## 10) Quick checklist (common problems)

- `google-services.json` is exactly at `app/google-services.json`
- Firebase Console has:
  - Auth → Email/Password enabled
  - Firestore created
  - Storage created
- You’re signed in with a real account in the app before posting/browsing
- Internet permission exists (already added in `AndroidManifest.xml`)

