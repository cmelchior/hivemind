plugins {
    kotlin("jvm") version "2.4.20"
    application
}

group = "dk.ilios.hivemind"
version = "1.0-SNAPSHOT"

kotlin {
    jvmToolchain(21)
}

application {
    mainClass = "dk.ilios.hivemind.MainParseGames"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(fileTree("libs") { include("*.jar") })
    testImplementation("junit:junit:4.13.1")
}
