package hkhc.electricspock

import hkhc.electricspock.internal.ContainedRobolectricTestRunner
import hkhc.electricspock.internal.ElectricSpockInterceptor
import org.junit.platform.runner.JUnitPlatform
import org.junit.runner.Description
import org.junit.runner.Runner
import org.junit.runner.manipulation.Filter
import org.junit.runner.manipulation.Filterable
import org.junit.runner.manipulation.Sortable
import org.junit.runner.manipulation.Sorter
import org.junit.runner.notification.RunNotifier
import org.spockframework.runtime.model.SpecInfo
import spock.lang.Specification
import spock.lang.Title
import java.lang.reflect.Constructor
import java.lang.reflect.Method

class ElectricSputnik(
    specClass: Class<out Specification>
) : Runner(), Filterable {

    private val containedRunner = ContainedRobolectricTestRunner(specClass)
    private val androidSandbox = containedRunner.getContainedAndroidSandbox()
    private val specInfoClass = androidSandbox.bootstrappedClass<SpecInfo>(SpecInfo::class.java)

    private val runner = createRunner(specClass)

    init {
        registerSpec()
    }

    /**
     * Sputnik is the test runner for Spock specification. This method Load the spec class and
     * Sputnik class with Robolectric sandbox, so that Robolectric can intercept the Android API
     * code. That's how we bridge Spock framework and Robolectric together.
     *
     * @param specClass the Specification class to be run under Sputnik
     */
    private fun createRunner(specClass: Class<out Specification>): Runner {
        val bootstrappedTestClass = androidSandbox.bootstrappedClass<Specification>(specClass)
        return androidSandbox
            .bootstrappedClass<Any>(JUnitPlatform::class.java) // fixme: or Suite
            .getConstructor(Class::class.java)
            .newInstance(bootstrappedTestClass)
                as Runner
    }

    /**
     * Register an interceptor to specInfo of every method in specification.
     */
    private fun registerSpec() {
        val interceptorConstructor = getInterceptorConstructor()

        runner.javaClass.declaredMethods.forEach { method ->
            val specInfo = getSpec(method)
            if (specInfo != null) {
                // ElectricSpockInterceptor register itself to SpecInfo on construction,
                // no need to keep a ref here
                interceptorConstructor.newInstance(specInfo, containedRunner)
            }
        }
    }

    /**
     * Get a sandboxed constructor of interceptor
     */
    private fun getInterceptorConstructor(): Constructor<*> {
        return androidSandbox
            .bootstrappedClass<ElectricSpockInterceptor>(ElectricSpockInterceptor::class.java)
            .declaredConstructors.get(0)
//            .getConstructor(
//                specInfoClass,
//                ContainedRobolectricTestRunner::class.java
//            )
    }

    private fun getSpec(method: Method): SpecInfo? {
        return if (method.name.equals("getSpec")) {
            method.isAccessible = true

            val specInfo = method.invoke(runner)
            if (specInfo.javaClass != specInfoClass) {
                throw RuntimeException("Failed to obtain SpecInfo instance from getSpec method." +
                " Instance of '${specInfo.javaClass.name}' is obtained")
            }
            specInfo as SpecInfo
        } else {
            null
        }
    }

    override fun getDescription(): Description {
        val originalDesc = runner.description
        val testClass = originalDesc.testClass
            ?: throw RuntimeException("Unexpected null testClass")

        var title: String? = null
        val annotations = testClass.annotations
        annotations.forEach { annotation ->
            if (annotation is Title) {
                title = annotation.value
                return@forEach
            }
        }

        val overridedDesc = Description.createSuiteDescription(
            if (title == null) testClass.name else title
        )
        originalDesc.children.forEach { child ->
            overridedDesc.addChild(child)
        }

        return overridedDesc
    }

    override fun run(notifier: RunNotifier?) {
        runner.run(notifier)
    }

    override fun filter(filter: Filter?) {
        (runner as Filterable).filter(filter)
    }
}