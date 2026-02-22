import org.apache.commons.csv.CSVFormat
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.io.FileReader

abstract class CompareCoverageCsvTask : DefaultTask() {
    @get:InputFile
    @get:Optional
    abstract val baseCsvData: RegularFileProperty

    @get:InputFile
    abstract val currentCsvData: RegularFileProperty

    @get:OutputFile
    @get:Optional
    abstract val baseNewCsvData: RegularFileProperty

    @TaskAction
    fun compare() {
        val baseCsv = baseCsvData.orNull?.asFile
        val currentCsv = currentCsvData.get().asFile
        val baseNewCsv = baseNewCsvData.get().asFile

        if (!currentCsv.exists()) {
            throw GradleException("Current coverage file not found at ${currentCsv.absolutePath}")
        }

        if (baseCsv == null || !baseCsv.exists()) {
            baseNewCsv.parentFile.mkdir()
            baseNewCsv.writeText(currentCsv.readText())
            println("No baseline found. Wrote current coverage as new baseline to ${baseNewCsv.absolutePath}")
            return
        }

        val baseCoverages = readCoverageCsvFile(baseCsv)
        val currentCoverages = readCoverageCsvFile(currentCsv)

        var improved = false
        var worsened = false
        val messages = mutableListOf<String>()

        baseCoverages.forEach { (module, baseCoverage) ->
            val currentCoverage = currentCoverages[module]
            if (currentCoverage == null) {
                worsened = true
                messages.add("Module missing: $module")
            } else {
                if (currentCoverage.decreasedFrom(baseCoverage)) {
                    worsened = true;
                    messages.add("Coverage decreased for $module: $baseCoverage -> $currentCoverage")
                }
            }
        }

        if (worsened)  {
            messages.forEach { println(it) }
            throw GradleException("Coverage worsened. See the above messages.")
        }

        currentCoverages.forEach { (module, currentCoverage) ->
            val baseCoverage = baseCoverages[module]
            if (baseCoverage == null) {
                improved = true
                messages.add("Module added: $module")
            } else {
                if (currentCoverage.increasedFrom(baseCoverage)) {
                    improved = true;
                    messages.add("Coverage increased for $module: $baseCoverage -> $currentCoverage")
                }
            }
        }

        if (improved) {
            messages.forEach { println(it) }
            baseNewCsv.parentFile.mkdir()
            baseNewCsv.writeText(currentCsv.readText())
            println("🎉 Coverage improved. Wrote new baseline to ${baseNewCsv.absolutePath}")
        } else {
            println("✔️ No change in coverage data")
        }
    }

    private fun readCoverageCsvFile(csvFile: File): Map<String, Coverage> {
        if (!csvFile.exists()) {
            return mapOf()
        }

        val reader = FileReader(csvFile)
        val result = mutableMapOf<String, Coverage>()
        reader.use { reader ->
            val records = (CSVFormat.DEFAULT.withFirstRecordAsHeader()).parse(reader)
            records.forEach { record ->
                val module = record.get("Module")
                val instructionCoverage = record.get("InstructionCoverage").toDouble()
                val branchCoverage = record.get("BranchCoverage").toDouble()
                val lineCoverage = record.get("LineCoverage").toDouble()
                val complexityCoverage = record.get("ComplexityCoverage").toDouble()
                val methodCoverage = record.get("MethodCoverage").toDouble()
                val coverage = Coverage(
                    instructionCoverage,
                    branchCoverage,
                    lineCoverage,
                    complexityCoverage,
                    methodCoverage
                )
                result[module] = coverage
            }
        }
        return result
    }
}