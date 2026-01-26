plugins {
    java
    jacoco
}

repositories {
    gradlePluginPortal()
}

allprojects {
    apply(plugin = "jacoco")

    repositories {
        gradlePluginPortal()
        mavenCentral()
    }

    tasks.withType<Test>().configureEach {
        finalizedBy(tasks.named("jacocoTestReport"))
    }

//    Uncomment the following to generate jacoco report for each module
//    tasks.withType<JacocoReport>().configureEach {
//        dependsOn("test")
//        reports {
//            html.required = true
//            xml.required = true
//            csv.required = true
//        }
//    }
}

tasks.register<JacocoReport>("jacocoRootReport") {
    reports {
        html.required = true
        xml.required = true
        csv.required = true
    }
}

class JacocoRootReportConfigAction: Action<TaskExecutionGraph> {
    override fun execute(taskGraph: TaskExecutionGraph): Unit {
        if (taskGraph.hasTask(":jacocoRootReport")) {

            tasks.named<JacocoReport>("jacocoRootReport") {

                val execFiles = mutableListOf<File>()
                val sourceDirs = mutableListOf<File>()
                val classDirs = mutableListOf<File>()

                subprojects.forEach { subproject ->

                    val execFile =
                        file("${subproject.projectDir.absolutePath}/build/jacoco/test.exec")

                    if (execFile.exists() && execFile.isFile) {
                        execFiles.add(execFile)
                    }

                    val hasMainSourceSet =
                        (subproject.plugins.hasPlugin("java")
                                || subproject.plugins.hasPlugin("java-library")) &&
                                subproject.extensions.findByType(SourceSetContainer::class.java)
                                    ?.findByName("main") != null

                    if (hasMainSourceSet) {
                        val sourceSets =
                            subproject.extensions.getByType(SourceSetContainer::class.java)

                        val mainSourceSet = sourceSets.getByName("main")

                        mainSourceSet.allSource.srcDirs.forEach { srcDir ->
                            if (srcDir.exists()) {
                                sourceDirs.add(srcDir)
                            }
                        }

                        mainSourceSet.output.classesDirs.files.forEach { classDir ->
                            if (classDir.exists() && classDir.isDirectory) {
                                classDirs.add(classDir)
                            }
                        }
                    }
                }

                println("----------- Execution Files ---------------")
                execFiles.forEach { println(it) }
                println("----------- Source dirs ---------------")
                sourceDirs.forEach { println(it) }
                println("----------- Class dirs ---------------")
                classDirs.forEach { println(it) }


                executionData.setFrom(files(execFiles))

                if (sourceDirs.isNotEmpty()) {
                    sourceDirectories.setFrom(files(sourceDirs))
                }

                if (classDirs.isNotEmpty()) {
                    classDirectories.setFrom(
                        classDirs.map { dir ->
                            fileTree(dir) {
                                exclude(
                                    "**/generated/**",
                                    "**/bin/main/**",
                                    "**/protobuf/**",
                                    "**/proto/**",
                                    "**/test/**"
                                )
                            }
                        }
                    )
                }
            }
        }
    }
}

gradle.taskGraph.whenReady(JacocoRootReportConfigAction())

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}
