import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import kotlin.jvm.java

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
}

kotlin {
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    macosArm64() {
        binaries {
            executable() {
                entryPoint = "org.example.project.main"
            }
        }
    }
    
    jvm()
    
    js {
        browser()
        binaries.executable()
    }
    
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        binaries.executable()
    }
    
    sourceSets {
        commonMain.dependencies {
            val version = "9999.0.0-SNAPSHOT"
            implementation("org.jetbrains.compose.runtime:runtime:$version")
            implementation("org.jetbrains.compose.foundation:foundation:$version")
            implementation("org.jetbrains.compose.material3:material3:$version")
            implementation("org.jetbrains.compose.ui:ui:$version")
            implementation("org.jetbrains.compose.components:components-resources:1.10.0-beta02")
            implementation("org.jetbrains.compose.ui:ui-tooling-preview:1.10.0-beta02")
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutinesSwing)
        }
        webMain.dependencies {
            // implementation("org.jetbrains.kotlinx:kotlinx-browser:0.5.0")
        }
    }
}


compose.desktop {
    application {
        mainClass = "org.example.project.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "org.example.project"
            packageVersion = "1.0.0"
        }
    }
}

// ____________________ SPECIAL _____________________

project.extensions.configure(KotlinMultiplatformExtension::class.java) {
    compilerOptions {
        // skip prerelase check because `lib` is using -XXLanguage:+ExportKlibToOlderAbiVersion
        freeCompilerArgs.add("-Xskip-prerelease-check")
    }
}

// We substitute the kotlin-stdlib version here, because `lib` brings a transitive dependency on a newer stdlib (newer ABI)
project.configurations.configureEach {
    resolutionStrategy.eachDependency {
        if (requested.group == "org.jetbrains.kotlin" && requested.name.startsWith("kotlin-stdlib")) {
            useVersion("2.2.21")
        }
    }
}

// ________________________________________________