import org.gradle.api.Project
import org.gradle.kotlin.dsl.register

fun Project.configureAggregateCoverageTask() {
    tasks.register<AggregateCoverageTask>("aggregateCoverageCsv") {
        val subprojectCsvs = rootProject.subprojects.associate {
            it.name to it.file("build/reports/jacoco/test/jacocoTestReport.csv")
        }
        csvData.set(subprojectCsvs)
        csvFiles.from(subprojectCsvs.values)
        outputFile.set(rootProject.layout.projectDirectory.file("coverage/current.csv"))
    }
}
