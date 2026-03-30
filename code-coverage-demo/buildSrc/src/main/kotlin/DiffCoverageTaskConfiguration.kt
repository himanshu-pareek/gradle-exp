import org.gradle.api.Action
import org.gradle.api.Project

fun Project.configureDiffCoverage(){
    tasks.register(
        "diffCoverage",
        DiffCoverageTask::class.java,
        object: Action<DiffCoverageTask> {
            override fun execute(task: DiffCoverageTask) {
                println("============================ SOME MESSAGE FOR YOU ===================================")
                task.group = "verification"
                task.description = "Reports coverage on changed lines only (requires diff-cover: pip install diff-cover"
                task.dependsOn(":jacocoRootReport")
                task.xmlFile.set(project.layout.buildDirectory.file("reports/jacoco/jacocoRootReport/jacocoRootReport.xml"))
                task.markdownFile.set(project.layout.buildDirectory.file("reports/diff-coverage.md"))
                task.htmlDir.set(project.layout.buildDirectory.dir("reports/diff-coverage-html"))
                val srcRoots = CoverageHelper.collectSourceDirectories(project)
                    .map { it.toPath() }
                    .map { project.projectDir.toPath().relativize(it).toString() }
                task.srcRoots.set(srcRoots)
                val branch = project.findProperty("diffCoverage.compareBranch")
                if (branch != null) {
                    task.compareBranch.set(branch.toString())
                }
                val threshold = project.findProperty("diffCoverage.failUnder")
                if (threshold != null) {
                    task.failUnderPercent.set(threshold.toString().toInt())
                }
            }
        },
    )
}
