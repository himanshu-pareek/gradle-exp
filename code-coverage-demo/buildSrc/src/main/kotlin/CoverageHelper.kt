import org.gradle.api.Project
import org.gradle.api.tasks.SourceSetContainer
import java.io.File

class CoverageHelper {
    companion object {
        fun hasMainSourceSet(project: Project): Boolean {
            return (project.plugins.hasPlugin("java") || project.plugins.hasPlugin("java-library")) &&
                    project.extensions.findByType(SourceSetContainer::class.java)?.findByName("main") != null;
        }

        fun collectTestExecFiles(project: Project): List<File> {
            return project.subprojects.mapNotNull { it.file("${it.buildDir}/jacoco/test.exec") }
                .filter {it.exists() && it.isFile }
        }

        fun collectSourceDirectories(project: Project): List<File> {
            return project.subprojects.asSequence().filter { hasMainSourceSet(it) }
                .map { it.extensions.getByType(SourceSetContainer::class.java) }
                .map { it.getByName("main") }
                .map { it.allSource }
                .flatMap { it.srcDirs }
                .filter { it.exists() }.toList()
        }

        fun collectClassDirectories(project: Project): List<File> {
            return project.subprojects.asSequence().filter { hasMainSourceSet(it) }
                .map { it.extensions.getByType(SourceSetContainer::class.java) }
                .map { it.getByName("main") }
                .map { it.output }
                .map { it.classesDirs }
                .flatMap { it.files }
                .filter { it.exists() }
                .filter { it.isDirectory }
                .toList()
        }
    }
}