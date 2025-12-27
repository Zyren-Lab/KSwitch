import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm") version "2.1.0"
    id("org.jetbrains.kotlin.plugin.compose") version "2.1.0"
    id("org.jetbrains.compose") version "1.7.1"
}

group = "com.kswitch"
version = "1.0.3"

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google()
}

dependencies {
    implementation(compose.desktop.currentOs)
    implementation(compose.materialIconsExtended)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.9.0") // For Dispatchers.Main
}

compose {
    desktop {
        application {
            mainClass = "MainKt"
            nativeDistributions {
                targetFormats(TargetFormat.Deb, TargetFormat.Rpm, TargetFormat.Exe, TargetFormat.Pkg)
                packageName = "KSwitch"
                packageVersion = "1.0.3"
                description = "KSwitch - Android Backup Tool"
                vendor = "KSwitch"
                
                linux {
                    appCategory = "Utility"
                    iconFile.set(project.file("src/main/resources/icon.png"))
                    shortcut = true
                }
            }
        }
    }
}

kotlin {
    jvmToolchain(17)
}