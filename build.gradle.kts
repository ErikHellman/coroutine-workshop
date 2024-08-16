plugins {
    kotlin("jvm") version "2.0.0"
}

group = "se.hellsoft.kotlin"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(platform("com.squareup.okhttp3:okhttp-bom:4.12.0"))
    implementation("com.squareup.okhttp3:okhttp")
    implementation("com.squareup.okhttp3:logging-interceptor")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime-jvm:0.6.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:1.9.0-RC.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-test:1.9.0-RC.2")
    testImplementation("app.cash.turbine:turbine:1.1.0")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}