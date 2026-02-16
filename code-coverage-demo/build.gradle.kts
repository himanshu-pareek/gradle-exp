import java.io.FileReader
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser

plugins {
    java
}

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("org.apache.commons:commons-csv:1.4")
    }
}

repositories {
    gradlePluginPortal()
}

allprojects {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

apply (from = "coverage/coverage.gradle.kts")

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}
