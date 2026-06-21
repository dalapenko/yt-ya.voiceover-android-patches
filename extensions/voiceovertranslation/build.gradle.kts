dependencies {
    compileOnly(project(":extensions:shared-stubs:library"))
    compileOnly(libs.annotation)
}

android {
    namespace = "app.revanced.extension.youtube.patches.voiceovertranslation"
    defaultConfig {
        minSdk = 26
    }
}
