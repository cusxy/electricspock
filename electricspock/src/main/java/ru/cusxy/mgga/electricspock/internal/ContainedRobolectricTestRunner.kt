package ru.cusxy.mgga.electricspock.internal

import org.junit.runners.model.FrameworkMethod
import org.robolectric.RobolectricTestRunner
import org.robolectric.internal.AndroidSandbox
import org.robolectric.internal.bytecode.InstrumentationConfiguration
import java.lang.reflect.Method

/**
 * Modified RobolectricTestRunner solely to be used by Spock interceptor.
 *
 * Pretend to be a test runner for the placeholder test class. We don't actually run tat test method.
 * Just use it to trigger all initialization of Robolectric infrastructure, and use it to run Spock specification.
 */
internal class ContainedRobolectricTestRunner : RobolectricTestRunner(PlaceholderTest::class.java) {

    val sdkSandbox: AndroidSandbox

    private val placeholderMethod: FrameworkMethod = children[0]
    private val bootstrappedMethod: Method

    init {

        sdkSandbox = getSandbox(placeholderMethod)
        configureSandbox(sdkSandbox, placeholderMethod)

        val bootstrappedTestClass = sdkSandbox.bootstrappedClass<Any>(testClass.javaClass)
        bootstrappedMethod = bootstrappedTestClass.getMethod(placeholderMethod.method.name)
    }

    fun containedBeforeTest() {
        beforeTest(sdkSandbox, placeholderMethod, bootstrappedMethod)
    }

    fun containedAfterTest() {
        afterTest(placeholderMethod, bootstrappedMethod)
    }

    /* Override to add itself to doNotAcquireClass, so as to avoid classloader conflict */
    override fun createClassLoaderConfig(method: FrameworkMethod?): InstrumentationConfiguration {
        return InstrumentationConfiguration.Builder(super.createClassLoaderConfig(method))
            .doNotAcquireClass(javaClass)
            .build()
    }
}