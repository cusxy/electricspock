package hkhc.electricspock.internal

import org.junit.Test
import org.junit.runners.model.FrameworkMethod
import org.robolectric.RobolectricTestRunner
import org.robolectric.internal.AndroidSandbox
import org.robolectric.internal.bytecode.InstrumentationConfiguration
import java.lang.reflect.Method

/**
 * Modified RobolectricTestRunner solely to be used by Spock interceptor.
 */

/**
 * Pretend to be a test runner for the placeholder test class. We don't actually run tat test method.
 * Just use it to trigger all initialization of Robolectric infrastructure, and use it to run Spock specification.
 */
internal class ContainedRobolectricTestRunnerV2 : RobolectricTestRunner(PlaceholderTest::class.java) {

    val placeholderMethod: FrameworkMethod
    val containedAndroidSandbox: AndroidSandbox
    val bootstrappedMethod: Method

    init {
        placeholderMethod = children[0]

        containedAndroidSandbox = getSandbox(placeholderMethod)
        configureSandbox(containedAndroidSandbox, placeholderMethod)

        val bootstrappedTestClass = containedAndroidSandbox.bootstrappedClass<Any>(testClass.javaClass)
        bootstrappedMethod = bootstrappedTestClass.getMethod(placeholderMethod.method.name)
    }

    fun containedBeforeTest() {
        beforeTest(containedAndroidSandbox, placeholderMethod, bootstrappedMethod)
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

    /**
     * A placeholder test class to obtain a proper FrameworkMethod (which is actually a
     * RoboFrameworkTestMethod) by reusing existing code in RobolectricTestRunner
     */
    class PlaceholderTest {
        /** Just a placeholder, the actual content of the test method is not important */
        @Test
        fun testPlaceholder() {
        }
    }
}