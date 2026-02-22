import org.gradle.api.Project
import org.gradle.kotlin.dsl.register

fun Project.configureCompareCoverageCsvTask() {
    tasks.register<CompareCoverageCsvTask>("compareCoverageCsvTask") {
        dependsOn("aggregateCoverageCsv")
        val baseFile = rootProject.layout.projectDirectory.file("coverage/base.csv")
        if (baseFile.asFile.exists()) {
            baseCsvData.set(baseFile)
        }
        currentCsvData.set(rootProject.layout.projectDirectory.file("coverage/current.csv"))
        baseNewCsvData.set(rootProject.layout.projectDirectory.file("coverage/base_new.csv"))
    }
}