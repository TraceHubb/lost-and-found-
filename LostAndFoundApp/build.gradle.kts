import java.io.File

// Top-level build file
plugins {
    id("com.android.application") version "8.3.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.20" apply false
    id("com.google.gms.google-services") version "4.4.4" apply false
}

// Keep build outputs outside OneDrive to avoid Windows file-lock/snapshot issues.
val externalBuildRoot = File(
    System.getenv("LOCALAPPDATA") ?: System.getProperty("java.io.tmpdir"),
    "LostAndFoundBuild"
)

allprojects {
    layout.buildDirectory.set(
        File(externalBuildRoot, project.path.replace(":", File.separator))
    )
}
