val kotlin_version: String by project
val logback_version: String by project

plugins {
    kotlin("jvm") version "2.1.20"
    id("io.ktor.plugin") version "3.2.3"
    kotlin("plugin.serialization") version "2.1.20"
}

application {
    mainClass.set("io.ktor.samples.kodein.KodeinSimpleApplicationKt")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.kotlin.gradlePlugin)
    implementation("io.ktor:ktor-server-core-jvm")
    implementation("io.ktor:ktor-server-netty-jvm")
    implementation("io.ktor:ktor-server-resources")
    implementation("io.ktor:ktor-server-default-headers")
    implementation("io.ktor:ktor-server-html-builder")
    implementation("org.kodein.di:kodein-di-jvm:7.17.0")
    implementation("com.tomtom.sdk.routing:routing-impl:1.26.0-rc13")
    implementation("ch.qos.logback:logback-classic:$logback_version")
    testImplementation("io.ktor:ktor-server-test-host-jvm")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")
}