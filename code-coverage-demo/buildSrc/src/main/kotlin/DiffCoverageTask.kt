import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import org.gradle.process.ExecOperations
import java.io.ByteArrayOutputStream
import javax.inject.Inject

abstract class DiffCoverageTask: DefaultTask() {
    @get:InputFile
    abstract val xmlFile: RegularFileProperty

    @get:Input
    @get:Optional
    abstract val compareBranch: Property<String>

    @get:Input
    @get:Optional
    abstract val failUnderPercent: Property<Int>

    @get:Input
    abstract val srcRoots: ListProperty<String>

    @get:OutputFile
    abstract val markdownFile: RegularFileProperty

    @get:Internal
    abstract val htmlDir: DirectoryProperty

    @get:Internal
    abstract val gitRootDir: DirectoryProperty

    @get:Inject
    abstract val execOperations: ExecOperations

    @TaskAction
    fun run() {
        val xml = xmlFile.get().asFile
        if (!xml.exists()) {
            throw GradleException("XML file not found at ${xml.absolutePath}")
        }

        val mdFile = markdownFile.get().asFile
        mdFile.parentFile.mkdirs()

        val htmlOutput = htmlDir.get().asFile
        val htmlFile = htmlOutput.resolve("index.html")

        val branch = if (compareBranch.isPresent) compareBranch.get() else "origin/main"
        val threshold = if (failUnderPercent.isPresent) failUnderPercent.get() else 0

        val roots = srcRoots.get()

        val command = mutableListOf(
            "diff-cover",
            xml.absolutePath,
            "--compare-branch=$branch",
            "--diff-range-notation=..",
            "--format=html:${htmlFile.absolutePath},markdown:${mdFile.absolutePath}"
        )
        if (roots.isNotEmpty()) {
            command.add("--src-roots")
            command.addAll(roots)
        }

        if (threshold > 0) {
            command.add("--fail-under=${threshold}")
        }

        htmlOutput.mkdirs()

        logger.lifecycle("Running diff-cover...")

        val stdout = ByteArrayOutputStream()
        val stderr = ByteArrayOutputStream()
        val result = execOperations.exec {
            workingDir(gitRootDir.get().asFile)
            commandLine(command)
            standardOutput = stdout
            errorOutput = stderr
            isIgnoreExitValue = true
        }

        val output = stdout.toString().trim()
        val errorOutput = stderr.toString().trim()

        if (output.isNotEmpty()) {
            logger.lifecycle(output)
        }
        if (errorOutput.isNotEmpty()) {
            logger.warn(errorOutput)
        }

        if (!htmlFile.exists()) {
            htmlOutput.deleteRecursively()
        }

        if (result.exitValue != 0 && threshold > 0) {
            throw GradleException(
                "diff-cover failed: coverage on changed lines is below $threshold%. "
                + "See ${mdFile.absolutePath} for details."
            )
        }

        if (htmlFile.exists()) {
            logger.lifecycle("Diff coverage HTML report: ${htmlFile.absolutePath}")
        }
        logger.lifecycle("Diff coverage markdown: ${mdFile.absolutePath}")
    }
}