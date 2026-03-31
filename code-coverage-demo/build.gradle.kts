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

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
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
