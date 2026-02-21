import java.io.File
import java.io.FileReader
import java.io.FileWriter
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

abstract class AggregateCoverageTask : DefaultTask() {
    @get:Input
    abstract val csvData: MapProperty<String, File>

    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    @TaskAction
    fun aggregate() {
        val data = csvData.get()
        val out = outputFile.get().asFile
        val outputDir = out.parentFile

        println("Aggregating coverage CSV files from subprojects...")

        val subprojectsData = mutableListOf<Map<String, String>>()

        data.forEach { (subprojectName, csvFile) ->
            val coverage = parseJacocoCsv(csvFile)

            subprojectsData.add(
                mapOf(
                    Pair("Module", subprojectName),
                    Pair("InstructionCoverage", String.format("%.2f", coverage["instructionCoverage"])),
                    Pair("BranchCoverage", String.format("%.2f", coverage["branchCoverage"])),
                    Pair("LineCoverage", String.format("%.2f", coverage["lineCoverage"])),
                    Pair("ComplexityCoverage", String.format("%.2f", coverage["complexityCoverage"])),
                    Pair("MethodCoverage", String.format("%.2f", coverage["methodCoverage"]))
                )
            )
        }

        if (!outputDir.exists()) {
            outputDir.mkdirs()
        }

        val writer = FileWriter(out)
        writer.use { writer ->
            val csvWriter = CSVPrinter(writer, CSVFormat.DEFAULT.withHeader(
                "Module",
                "InstructionCoverage",
                "BranchCoverage",
                "LineCoverage",
                "ComplexityCoverage",
                "MethodCoverage"
            ))

            subprojectsData.forEach {
                csvWriter.printRecord(
                    it["Module"],
                    it["InstructionCoverage"],
                    it["BranchCoverage"],
                    it["LineCoverage"],
                    it["ComplexityCoverage"],
                    it["MethodCoverage"]
                )
                csvWriter.flush()
            }
        }

        println("Aggregated coverage CSV written to: ${out.absolutePath}")
        println("Total subprojects processed: ${subprojectsData.size}")
    }

    private fun parseJacocoCsv(csvFile: File): Map<String, Double> {
        val coverage = mutableMapOf(
            Pair("instructionCoverage", 0.0),
            Pair("branchCoverage", 0.0),
            Pair("lineCoverage", 0.0),
            Pair("complexityCoverage", 0.0),
            Pair("methodCoverage", 0.0)
        )

        if (!csvFile.exists()) {
            return coverage
        }

        val reader = FileReader(csvFile)
        reader.use { reader ->
            val records = (CSVFormat.DEFAULT.withFirstRecordAsHeader()).parse(reader)
            var instructionMissed = 0
            var instructionCovered = 0
            var branchMissed = 0
            var branchCovered = 0
            var lineMissed = 0
            var lineCovered = 0
            var complexityMissed = 0
            var complexityCovered = 0
            var methodMissed = 0
            var methodCovered = 0

            records.forEach {
                instructionMissed += (it.get("INSTRUCTION_MISSED") ?: "0").toInt()
                instructionCovered += (it.get("INSTRUCTION_COVERED") ?: "0").toInt()
                branchMissed += (it.get("BRANCH_MISSED") ?: "0").toInt()
                branchCovered += (it.get("BRANCH_COVERED") ?: "0").toInt()
                lineMissed += (it.get("LINE_MISSED") ?: "0").toInt()
                lineCovered += (it.get("LINE_COVERED") ?: "0").toInt()
                complexityMissed += (it.get("COMPLEXITY_MISSED") ?: "0").toInt()
                complexityCovered += (it.get("COMPLEXITY_COVERED") ?: "0").toInt()
                methodMissed += (it.get("METHOD_MISSED") ?: "0").toInt()
                methodCovered += (it.get("METHOD_COVERED") ?: "0").toInt()
            }

            // Calculate percentage
            val instructionTotal = instructionMissed + instructionCovered
            val branchTotal = branchMissed + branchCovered
            val lineTotal = lineMissed + lineCovered
            val complexityTotal = complexityMissed + complexityCovered
            val methodTotal = methodMissed + methodCovered

            coverage["instructionCoverage"] = if (instructionTotal == 0) {
                0.0
            } else {
                (instructionCovered.toDouble() / instructionTotal * 100)
            }
            coverage["branchCoverage"] = if (branchTotal == 0) {
                0.0
            } else {
                (branchCovered.toDouble() / branchTotal * 100)
            }
            coverage["lineCoverage"] = if (lineTotal == 0) {
                0.0
            } else {
                (lineCovered.toDouble() / lineTotal * 100)
            }
            coverage["complexityCoverage"] = if (complexityTotal == 0) {
                0.0
            } else {
                (complexityCovered.toDouble() / complexityTotal * 100)
            }
            coverage["methodCoverage"] = if (methodTotal == 0) {
                0.0
            } else {
                (methodCovered.toDouble() / methodTotal * 100)
            }
        }

        return coverage
    }
}
