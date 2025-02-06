package ru.cusxy.mgga.groovyandroid

import com.android.build.gradle.BaseExtension
import com.android.build.gradle.api.AndroidSourceSet
import com.android.build.gradle.api.BaseVariant
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.logging.Logging
import org.gradle.api.model.ObjectFactory
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.tasks.compile.GroovyCompile
import ru.cusxy.mgga.groovyandroid.utils.forAllAndroidVariants
import javax.inject.Inject

/**
 * `GroovyAndroidPlugin` is a custom Gradle plugin that enables the use of Groovy in Android
 * projects. It provides a way to compile Groovy code and integrate it into the Android build
 * process, even though official Groovy support has been deprecated by Apache.
 *
 * **Purpose:**
 *
 * The primary goal of this plugin is to allow developers to continue using Groovy in their
 * Android projects, particularly for writing tests with frameworks like Spock.
 *
 * **Warning:**
 *
 * **Android Build Integration is not supported.** It doesn't integrate the compiled Groovy code
 * into the Android build process, making it part of the final APK or AAB. The required version
 * of _grooid_ is no longer supported.
 */
class GroovyAndroidPlugin
@Inject constructor(
    private val objects: ObjectFactory,
    private val providerFactory: ProviderFactory,
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

            val groovySourceDirectorySet =
                provider.extensions.findByName("groovy") as? SourceDirectorySet ?: return@forEach

            groovyTask.source(groovySourceDirectorySet)

            // exclude any java files that may be included in both java and groovy source sets
            val sourceSetFiles = providerFactory.provider { groovySourceDirectorySet.files }
            javaTask.exclude { file ->
                file.file in sourceSetFiles.get()
            }
        }

        // no sources for groovy to compile skip the groovy task
        if (groovyTask.source.isEmpty) {
            logger.debug("No groovy sources found for {} removing groovy task", variantName)
            groovyTask.isEnabled = false
            return
        }
        logger.debug("Groovy sources for {}: {}", variantName, groovyTask.source.files)

        val androidRuntime = providerFactory.provider { androidExtension.bootClasspath }
        val groovyClasspath = providerFactory.provider {
            objects.fileCollection().from(androidRuntime.get(), javaTask.classpath)
        }
        val groovyCompilerArgs = providerFactory.provider { javaTask.options.compilerArgs }
        val groovyAnnotationProcessorPath = providerFactory.provider { javaTask.options.annotationProcessorPath }

        groovyTask.doFirst {
            this as GroovyCompile

            this.classpath = groovyClasspath.get()
            this.groovyClasspath = this.classpath
            this.options.compilerArgs.addAll(groovyCompilerArgs.get())
            this.groovyOptions.isJavaAnnotationProcessing = true
            this.options.annotationProcessorPath = groovyAnnotationProcessorPath.get()

            logger.debug("Java annotationProcessorPath {}", groovyAnnotationProcessorPath.get())
            logger.debug("Groovy compiler args {}", this.options.compilerArgs)
        }

        androidVariant.registerPostJavacGeneratedBytecode(groovyTask.outputs.files)
    }
}
