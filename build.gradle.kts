import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet

val ktor_version: String by project
val logback_version: String by project

plugins {
    kotlin("jvm") version "1.3.61"
    kotlin("plugin.serialization") version "1.3.61"
    application
}

application {
    mainClassName = "io.ktor.server.netty.EngineMain"
}

group = "me.kumatheta.feh"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    jcenter()
    maven("https://jitpack.io")
}

sourceSets["main"].withConvention(KotlinSourceSet::class) {
    kotlin.srcDir("src/main/kotlin-gen")
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("reflect"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.3")
    implementation("com.github.ben-manes.caffeine:caffeine:2.8.1")
    implementation("com.marcinmoskala:DiscreteMathToolkit:1.0.3")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime:0.14.0")
    implementation("io.ktor:ktor-server-netty:$ktor_version")
    implementation("ch.qos.logback:logback-classic:$logback_version")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}