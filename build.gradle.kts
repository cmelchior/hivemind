plugins {
    java
    application
}

group = "dk.ilios.hivemind"
version = "1.0-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
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
