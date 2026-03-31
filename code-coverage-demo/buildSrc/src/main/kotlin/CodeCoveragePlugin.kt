import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create

class CodeCoveragePlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.extensions.create<CodeCoverageExtension>("codeCoverage")
        project.configureJacoco()
        project.configureDiffCoverage()
    }
}
