import org.gradle.kotlin.dsl.implementation

plugins {
    kotlin("jvm") version "2.1.21"
    id("io.ktor.plugin") version "3.2.3"
    kotlin("plugin.serialization") version "2.1.21"
}

application {
    mainClass.set("io.ktor.MainKt")
    // Debug print to check if properties are loaded
}

dependencies {
    implementation(libs.kotlin.gradlePlugin)
    implementation("io.ktor:ktor-server-core-jvm")
    implementation("io.ktor:ktor-server-netty-jvm")
    implementation("io.ktor:ktor-server-resources")
    implementation("io.ktor:ktor-server-default-headers")
    implementation("io.ktor:ktor-server-html-builder")
    implementation("org.kodein.di:kodein-di-jvm:7.17.0")
    implementation(libs.logback.classic)
    implementation(libs.airship)
    testImplementation("io.ktor:ktor-server-test-host-jvm")
    testImplementation(libs.kotlin.test)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.json)
    implementation(libs.ktor.serialization)
}