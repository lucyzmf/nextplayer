import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.kotlinSerialization)
}

tasks.withType<JavaCompile> {
    sourceCompatibility = libs.versions.android.jvm.get()
    targetCompatibility = libs.versions.android.jvm.get()
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = libs.versions.android.jvm.get()
    }
}

dependencies {
    implementation(libs.kotlinx.serialization.json)
}
