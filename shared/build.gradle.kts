import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.googleServices)
    kotlin("plugin.serialization") version "1.9.0"

}

kotlin {
    androidTarget {
        compilations.all {
            compileTaskProvider.configure {
                compilerOptions {
                    jvmTarget.set(JvmTarget.JVM_17)
                }
            }
        }
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "shared"
            isStatic = true
        }
    }

    sourceSets {
        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
            implementation(platform("com.google.firebase:firebase-bom:33.13.0"))
            implementation(libs.ktor.client.android)
        }

        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
        commonMain.dependencies {
            //put your multiplatform dependencies here
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.core.i18n)
            implementation(libs.firebase.firestore)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.coil.network.ktor)
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
            implementation("io.github.vanpra.compose-material-dialogs:datetime:0.9.0")
            implementation("org.jetbrains.androidx.navigation:navigation-compose:2.9.0-alpha16")
            implementation("io.coil-kt.coil3:coil-compose:3.2.0")
            implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.5.0")
            implementation("io.github.vanpra.compose-material-dialogs:core:0.9.0")
            implementation("com.russhwolf:multiplatform-settings:1.0.0-RC")
            implementation("network.chaintech:cmp-preference:1.0.0")
            implementation("dev.gitlive:firebase-common:2.1.0")
            api(libs.datastore.preferences)
            implementation(compose.materialIconsExtended)

        }
        val iosSimulatorArm64Main by getting {
            dependencies {
                implementation(compose.material3) // material3 from Compose 1.8.1
            }
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

android {
    namespace = "com.salesflo.snackflo"
    compileSdk = 35
    defaultConfig {
        minSdk = 24
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
