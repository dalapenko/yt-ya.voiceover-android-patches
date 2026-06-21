group = "app.revanced"

patches {
    about {
        name = "Yandex Voice Over Translation Patch"
        description = "Adds Yandex Voice Over Translation to YouTube."
        source = "https://github.com/dalapenko/revance-yt-voiceover"
        author = "dalapenko"
        contact = "dalapenko.dev@gmail.com"
        website = "https://revanced.app"
        license = "GNU General Public License v3.0"
    }
}

dependencies {
    implementation(libs.guava)
    implementation(libs.apksig)
    compileOnly(libs.patches)
    compileOnly(project(":patches:stub"))
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll(
            "-Xexplicit-backing-fields",
            "-Xcontext-parameters"
        )
    }
}

tasks.named<Jar>("jar") {
    manifest {
        attributes["Version"] = "6.2.1"
    }
}

