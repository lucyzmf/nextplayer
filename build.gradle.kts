import com.mikepenz.aboutlibraries.plugin.DuplicateMode
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.gradle.api.tasks.testing.logging.TestExceptionFormat

plugins {
    alias(libs.plugins.aboutLibraries) apply false
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.kotlinAndroid) apply false
    alias(libs.plugins.kotlinJvm) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.ktlint) apply false
}

subprojects {
    // Apply the Kotlin JVM plugin to subprojects that need it
    pluginManager.withPlugin("org.jetbrains.kotlin.jvm") {
        configure<org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension> {
            jvmToolchain(23) // Set the desired JVM version here (e.g., 21)
        }
    }
    // Apply the Kotlin Android plugin to subprojects that need it
    pluginManager.withPlugin("org.jetbrains.kotlin.android") {
        configure<org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension> {
            jvmToolchain(23)
        }
    }
    // Apply the Java plugin to subprojects that need it
    pluginManager.withPlugin("java") {
        tasks.withType<JavaCompile>().configureEach {
            sourceCompatibility = "23"
            targetCompatibility = "23"
        }
    }
}
subprojects {
    apply(plugin = "org.jlleitschuh.gradle.ktlint")
    apply(plugin = "com.mikepenz.aboutlibraries.plugin")

    configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
        android.set(true)
        outputColorName.set("RED")
        ignoreFailures.set(false)
    }

    configure<com.mikepenz.aboutlibraries.plugin.AboutLibrariesExtension> {
        // Remove the "generated" timestamp to allow for reproducible builds
        excludeFields = arrayOf("generated")
        duplicationMode = DuplicateMode.MERGE
    }
}

allprojects {
    tasks.withType<Test>().configureEach {
        testLogging {
            events = setOf(
                TestLogEvent.PASSED,
                TestLogEvent.FAILED,
                TestLogEvent.SKIPPED,
                TestLogEvent.STANDARD_OUT,
                TestLogEvent.STANDARD_ERROR
            )

            exceptionFormat = TestExceptionFormat.FULL
            showExceptions = true
            showCauses = true
            showStackTraces = true
        }
        ignoreFailures = true
    }
}
