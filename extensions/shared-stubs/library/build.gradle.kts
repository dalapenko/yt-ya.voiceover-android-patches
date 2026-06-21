plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "app.revanced.extension.shared"
    compileSdk = 34

    defaultConfig {
        minSdk = 24
    }
}

dependencies {
    compileOnly(libs.annotation)
}
