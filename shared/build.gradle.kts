import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
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

    val iosTargets = listOf(iosArm64(), iosX64(), iosSimulatorArm64())
    iosTargets.forEach { target ->
        target.binaries {
            executable {
                // Set minimum iOS version to 14.1
                linkerOpts("-ios_version_min 14.1")
            }
            // For framework builds (if needed)
            framework {
                baseName = "shared"
                isStatic = true
                linkerOpts("-ios_version_min 14.1")
            }
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
            implementation(libs.multiplatform.settings)
        }

        iosSimulatorArm64Main.dependencies {
            implementation(compose.material3) // material3 from Compose 1.8.1
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        commonMain.dependencies {
            //put your multiplatform dependencies here
            // Compose Multiplatform
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(compose.materialIconsExtended)

            // AndroidX
            implementation(libs.androidx.core.i18n)
            implementation(libs.androidx.navigation.compose)
            /*implementation(libs.androidx.core.i18n)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.androidx.navigation.compose)*/

            // Firebase (GitLive KMP)
            implementation(libs.firebase.firestore)
            implementation(libs.firebase.common)

            // Coil (Ktor integration)
            implementation(libs.coil.network.ktor)
            implementation(libs.coil.compose)

            // Kotlinx
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.datetime)

            // Compose Material Dialogs
            implementation(libs.compose.material.dialogs.core)
            implementation(libs.compose.material.dialogs.datetime)

            // Multiplatform Settings
            implementation(libs.multiplatform.settings)

            // DataStore
            api(libs.datastore.preferences)

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
