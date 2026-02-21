import java.io.FileReader
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import java.io.FileWriter

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("org.apache.commons:commons-csv:1.4")
    }
}

plugins {
    java
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

apply<CodeCoveragePlugin>()

configure<CodeCoverageExtension> {
    excludes.addAll(
        listOf(
            "**/generated/**",
            "**/bin/main/**",
            "**/protobuf/**",
            "**/proto/**",
            "**/test/**"
        )
    )
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}
