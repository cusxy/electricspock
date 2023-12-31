package plugin

import com.android.build.gradle.BaseExtension
import com.android.build.gradle.api.AndroidSourceSet
import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.api.SourceKind
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.logging.Logging
import org.gradle.api.model.ObjectFactory
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.tasks.compile.GroovyCompile
import plugin.utils.forAllAndroidVariants
import java.io.File
import javax.inject.Inject

class GroovyAndroidPlugin
@Inject constructor(
    private val objects: ObjectFactory,
) : Plugin<Project> {

    private val logger = Logging.getLogger(this::class.java)

    override fun apply(project: Project) {
        configureProject(project)
    }

    private fun configureProject(project: Project) {
        val androidPlugin = project.plugins.findPlugin("com.android.base")

        logger.debug("Found Plugin: {}", androidPlugin)

        if (androidPlugin == null) {
            throw GradleException("You must apply the Android plugin or the Android library plugin before using the groovy-android plugin")
        }

        val androidExtension = project.extensions.getByName("android") as BaseExtension
        androidExtension.sourceSets.forEach { sourceSet ->
            configureSourceSet(project, sourceSet)
        }

        project.afterEvaluate {
            project.forAllAndroidVariants { variant ->
                configureVariant(project, androidExtension, variant)
            }
        }
    }

    private fun configureSourceSet(
        project: Project,
        sourceSet: AndroidSourceSet
    ) {
        if (sourceSet !is ExtensionAware) return

        val sourceSetName = sourceSet.name
        val sourceSetPath = project.file("src/$sourceSetName/groovy")

        if (!sourceSetPath.exists()) {
            logger.debug("SourceSet path does not exists for {} {}", sourceSetName, sourceSetPath)
            return
        }

        // add so Android Studio will recognize groovy files can see these
        sourceSet.java.srcDirs(sourceSetPath)

        val groovySourceDirectorySet = objects.sourceDirectorySet(
            "$sourceSetName Groovy source", "$sourceSetName Groovy source"
        )
        sourceSet.extensions.add("groovy", groovySourceDirectorySet)
        groovySourceDirectorySet.filter.include("**/*.java", "**/*.groovy")
        groovySourceDirectorySet.srcDir(sourceSetPath)

        logger.debug("Created groovy sourceDirectorySet at {}", groovySourceDirectorySet.srcDirs)
    }

    private fun configureVariant(
        project: Project,
        androidExtension: BaseExtension,
        androidVariant: BaseVariant
    ) {
        val variantName = androidVariant.name

        logger.debug("Processing variant {}", variantName)

        val javaTask = androidVariant.javaCompileProvider.get()

        val groovyTaskName = javaTask.name.replace("Java", "Groovy")
        val groovyTask = project.tasks.create(groovyTaskName, GroovyCompile::class.java)

        // do before configuration so users can override / don't break backwards compatibility
        groovyTask.targetCompatibility = javaTask.targetCompatibility
        groovyTask.sourceCompatibility = javaTask.sourceCompatibility

        groovyTask.destinationDirectory.set(
            project.file(javaTask.destinationDirectory.get().asFile.path.replace("java", "groovy"))
        )
        groovyTask.classpath = javaTask.classpath
        groovyTask.groovyClasspath = javaTask.classpath

        androidVariant.sourceSets.forEach { provider ->
            if (provider !is ExtensionAware) return@forEach

            val groovySourceDirectorySet = provider.extensions.findByName("groovy") as? SourceDirectorySet
            if (groovySourceDirectorySet == null) return@forEach

            groovyTask.source(groovySourceDirectorySet)

            // exclude any java files that may be included in both java and groovy source sets
            javaTask.exclude { file ->
                file.file in groovySourceDirectorySet.files
            }
        }

        // no sources for groovy to compile skip the groovy task
        if (groovyTask.source.isEmpty) {
            logger.debug("No groovy sources found for {} removing groovy task", variantName)
            groovyTask.isEnabled = false
            return
        }
        logger.debug("Groovy sources for {}: {}", variantName, groovyTask.source.files)

        groovyTask.doFirst { task ->
            task as GroovyCompile

            val androidRuntime = androidExtension.bootClasspath
            task.classpath = objects.fileCollection().from(androidRuntime, javaTask.classpath)
            task.groovyClasspath = task.classpath
            task.options.compilerArgs.addAll(javaTask.options.compilerArgs)
            task.groovyOptions.isJavaAnnotationProcessing = true
            task.options.annotationProcessorPath = javaTask.options.annotationProcessorPath

            logger.debug("Java annotationProcessorPath {}", javaTask.options.annotationProcessorPath)
            logger.debug("Groovy compiler args {}", task.options.compilerArgs)
        }

        androidVariant.registerPostJavacGeneratedBytecode(groovyTask.outputs.files)
    }

    private fun getGeneratedSourceDirs(variant: BaseVariant): List<File> {
        return variant.getSourceFolders(SourceKind.JAVA).map { it.dir }
    }
}
