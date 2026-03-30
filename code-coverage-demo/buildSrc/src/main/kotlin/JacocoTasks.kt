import org.gradle.api.Project
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.withType
import org.gradle.testing.jacoco.plugins.JacocoPlugin
import org.gradle.testing.jacoco.tasks.JacocoReport
import java.io.File

fun Project.configureJacoco() {
    val extension = extensions.getByType(CodeCoverageExtension::class.java)

    plugins.apply(JacocoPlugin::class.java)

    tasks.register("jacocoRootReport", JacocoReport::class.java) {
        reports {
            html.required.set(true)
            xml.required.set(true)
            csv.required.set(true)
        }
    }

    allprojects {
        apply(plugin = "jacoco")

        tasks.withType<Test>().configureEach {
            finalizedBy(tasks.named("jacocoTestReport"))
        }

        tasks.withType<JacocoReport>().configureEach {
            dependsOn("test")
            enabled = false
            reports {
                html.required.set(false)
                xml.required.set(false)
                csv.required.set(false)
            }
        }
    }

    gradle.taskGraph.whenReady {
        if (hasTask(":jacocoRootReport")) {
            tasks.named<JacocoReport>("jacocoRootReport") {
                val execFiles = mutableListOf<File>()
                val sourceDirs = mutableListOf<File>()
                val classDirs = mutableListOf<File>()

                subprojects.forEach { subproject ->
                    val execFile = subproject.file("${subproject.buildDir}/jacoco/test.exec")
                    if (execFile.exists() && execFile.isFile) {
                        execFiles.add(execFile)
                    }

                    val hasMainSourceSet = (subproject.plugins.hasPlugin("java") || subproject.plugins.hasPlugin("java-library")) &&
                            subproject.extensions.findByType(SourceSetContainer::class.java)?.findByName("main") != null

                    if (hasMainSourceSet) {
                        val sourceSets = subproject.extensions.getByType(SourceSetContainer::class.java)
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

                println("Execution files")
                execFiles.forEach {
                    println(it.absolutePath)
                }

                println("Source directories")
                sourceDirs.forEach {
                    println(it.absolutePath)
                }

                println("Class directories")
                classDirs.forEach {
                    println(it.absolutePath)
                }

                executionData.setFrom(files(execFiles))
                if (sourceDirs.isNotEmpty()) {
                    sourceDirectories.setFrom(files(sourceDirs))
                }
                if (classDirs.isNotEmpty()) {
                    classDirectories.setFrom(
                        classDirs.map { dir ->
                            fileTree(dir) {
                                exclude(extension.excludes)
                            }
                        }
                    )
                }
            }
        }
    }
}
