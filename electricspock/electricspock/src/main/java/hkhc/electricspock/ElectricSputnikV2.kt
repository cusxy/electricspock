package hkhc.electricspock

import hkhc.electricspock.internal.ContainedRobolectricTestRunnerV2
import hkhc.electricspock.internal.ElectricSpockInterceptor
import org.junit.platform.runner.JUnitPlatform
import org.junit.runner.Description
import org.junit.runner.Runner
import org.junit.runner.manipulation.Filter
import org.junit.runner.manipulation.Filterable
import org.junit.runner.notification.RunNotifier
import org.spockframework.runtime.model.SpecInfo
import spock.lang.Specification
import java.lang.reflect.Constructor
import java.lang.reflect.Method

class ElectricSputnikV2(
    specClass: Class<out Specification>
) : Runner(), Filterable {

    private val containedRunner = ContainedRobolectricTestRunnerV2()
    private val androidSandbox = containedRunner.containedAndroidSandbox
    private val specInfoClass = androidSandbox.bootstrappedClass<SpecInfo>(SpecInfo::class.java)

    private val delegate = createDelegate(specClass)

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
    private fun createDelegate(specClass: Class<out Specification>): Runner {
        val bootstrappedTestClass = androidSandbox.bootstrappedClass<Specification>(specClass)
        return androidSandbox
            .bootstrappedClass<Any>(JUnitPlatform::class.java)
            .getConstructor(Class::class.java)
            .newInstance(bootstrappedTestClass) as Runner
    }

    /**
     * Register an interceptor to specInfo of every method in specification.
     */
    private fun registerSpec() {
        val interceptorConstructor = getInterceptorConstructor()

        delegate.javaClass.declaredMethods.forEach { method ->
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
            .declaredConstructors[0]
    }

    private fun getSpec(method: Method): Any? {
        return if (method.name.equals("getSpec")) {
            method.isAccessible = true

            val specInfo = method.invoke(delegate)
            if (specInfo.javaClass != specInfoClass) {
                throw RuntimeException(
                    "Failed to obtain SpecInfo instance from getSpec method." +
                            " Instance of '${specInfo.javaClass.name}' is obtained"
                )
            }
            specInfo
        } else {
            null
        }
    }

    override fun getDescription(): Description = delegate.description

    override fun run(notifier: RunNotifier?): Unit = delegate.run(notifier)

    override fun filter(filter: Filter?): Unit = (delegate as Filterable).filter(filter)
}