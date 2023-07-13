package hkhc.electricspock.internal

import org.junit.Test
import org.junit.runners.model.FrameworkMethod
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.internal.AndroidSandbox
import org.robolectric.internal.bytecode.InstrumentationConfiguration
import spock.lang.Specification
import java.lang.reflect.Method

/**
 * Modified RobolectricTestRunner solely to be used by Spock interceptor.
 */

/**
 * Pretend to be a test runner for the placeholder test class. We don't actually run tat test method.
 * Just use it to trigger all initialization of Robolectric infrastructure, and use it to run Spock specification.
 */
class ContainedRobolectricTestRunner(
    private val specClass: Class<out Specification>
) : RobolectricTestRunner(PlaceholderTest::class.java) {

    private lateinit var placeholderMethod: FrameworkMethod
    private lateinit var androidSandbox: AndroidSandbox
    private lateinit var bootstrappedMethod: Method

    // todo: replace with lazy
    fun getPlaceholderMethod(): FrameworkMethod {
        if (!this::placeholderMethod.isInitialized) {
            placeholderMethod = children[0]
        }
        return placeholderMethod
    }

    // todo: implement getChildren method

    fun createBootstrappedMethod(): Method {
        val placeholderMethod = getPlaceholderMethod()
        val androidSandbox = getContainedAndroidSandbox()

        // getTestClass().getJavaClass() should always be PlaceholderTest.class,
        // load under Robolectric's class loader
        val bootstrappedTestClass = androidSandbox.bootstrappedClass<Any>(testClass.javaClass)
        // todo: replace with other fun
        return bootstrappedTestClass.getMethod(placeholderMethod.method.name)
    }

    fun getContainedAndroidSandbox(): AndroidSandbox {
        if (!this::androidSandbox.isInitialized) {
            androidSandbox = getSandbox(getPlaceholderMethod())
            configureSandbox(androidSandbox, getPlaceholderMethod()) // replaced
        }
        return androidSandbox
    }

    fun containedBeforeTest() {
        beforeTest(getContainedAndroidSandbox(), getPlaceholderMethod(), getBootstrappedMethod())
    }

    fun containedAfterTest() {
        afterTest(getPlaceholderMethod(), getBootstrappedMethod())
    }

    private fun getBootstrappedMethod(): Method {
        if (!this::bootstrappedMethod.isInitialized) {
            bootstrappedMethod = createBootstrappedMethod()
        }
        return bootstrappedMethod
    }

    /* Override to add itself to doNotAcquireClass, so as to avoid classloader conflict */
    override fun createClassLoaderConfig(method: FrameworkMethod?): InstrumentationConfiguration {
        InstrumentationConfiguration.Builder(super.createClassLoaderConfig(method))
            .doNotAcquireClass(javaClass) // fixme: тут точно java class?
            .build()
        return super.createClassLoaderConfig(method)
    }

    // todo: getConfig deprecated

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