import org.gradle.api.Project
import org.gradle.kotlin.dsl.register
import java.io.File

fun Project.configureAggregateCoverageTask() {
    tasks.register<AggregateCoverageTask>("aggregateCoverageCsv") {
        val subprojectCsvs = rootProject.subprojects.associate {
            it.name to it.file("build/reports/jacoco/test/jacocoTestReport.csv")
        }
        csvData.set(subprojectCsvs)
        outputFile.set(rootProject.layout.projectDirectory.file("coverage/current.csv"))
    }
}
