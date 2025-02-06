package ru.cusxy.mgga.electricspock

import org.junit.platform.engine.DiscoverySelector
import org.junit.platform.engine.EngineDiscoveryRequest
import org.junit.platform.engine.ExecutionRequest
import org.junit.platform.engine.TestDescriptor
import org.junit.platform.engine.TestEngine
import org.junit.platform.engine.UniqueId
import org.junit.platform.engine.discovery.ClassSelector
import org.spockframework.runtime.SpockEngine
import ru.cusxy.mgga.electricspock.internal.ContainedRobolectricTestRunner
import ru.cusxy.mgga.electricspock.internal.ProxyParentClassLoader
import ru.cusxy.mgga.electricspock.internal.utils.findSecurityProtectedField

/**
 * `ProxyTestEngine` is a custom JUnit Platform `TestEngine` that enables the use of the Spock testing
 * framework in conjunction with Robolectric for Android testing. It acts as a bridge between the
 * JUnit Platform, Spock, and Robolectric, overcoming their inherent incompatibilities.
 *
 * **Purpose:**
 *
 * The primary goal of `ProxyTestEngine` is to allow developers to write Android tests using Spock's
 * expressive syntax and Robolectric's simulated Android environment. This combination is not
 * natively supported because Spock and Robolectric use different test runners and class loading
 * mechanisms.
 *
 * **Functionality:**
 *
 * 1.  **Delegation to Spock:** `ProxyTestEngine` delegates the core test discovery and execution
 *     logic to the standard `SpockEngine`.
 * 2.  **Robolectric ClassLoader Integration:** It uses Robolectric's class loader to load test
 *     classes and their dependencies. This is crucial for Robolectric's shadow objects to function
 *     correctly.
 * 3.  **ClassLoader Manipulation:**
 *     *   It replaces the parent class loader of Robolectric's class loader with a custom
 *         `ProxyParentClassLoader`.
 *     *   It replaces the class loader of the test class with Robolectric's class loader.
 * 4. **Security Protected Field:**
 *     * It uses reflection to access a security-protected field.
 * 5. **Context ClassLoader:**
 *     * It changes the context class loader of the current thread.
 * 6. **Excluding Spock Engine:**
 *     * The user must exclude spock engine from JUnitPlatform.
 *
 * **How It Works:**
 *
 * *   **Test Discovery:** When the JUnit Platform starts test discovery, `ProxyTestEngine` intercepts
 *     the request. It then uses class loader manipulation to ensure that test classes are loaded
 *     by Robolectric's class loader. Finally, it delegates the discovery to the `SpockEngine`.
 * *   **Test Execution:** During test execution, `ProxyTestEngine` sets the current thread's context
 *     class loader to Robolectric's class loader. It then delegates the execution to the
 *     `SpockEngine`. After the execution, it restores the original context class loader.
 *
 * **Usage:**
 *
 * To use `ProxyTestEngine`, you need to:
 *
 * 1.  Include the `electricspock` library as a test dependency in your project.
 * 2.  Exclude the default `spock` engine from JUnit Platform in your `build.gradle.kts` file:
 *
 * ```kotlin
 * android {
 *     testOptions {
 *         unitTests {
 *             all { test ->
 *                 test.useJUnitPlatform {
 *                     excludeEngines("spock") // Exclude the default Spock engine
 *                 }
 *                 test.jvmArgs( // Required for advanced reflection
 *                     "--add-opens", "java.base/java.lang=ALL-UNNAMED",
 *                 )
 *             }
 *         }
 *     }
 * }
 * ```
 *
 * 3. Apply the `ru.cusxy.mgga.groovy-android` plugin to your module's `build.gradle.kts` file.
 *
 * @see SpockEngine
 * @see ContainedRobolectricTestRunner
 * @see ProxyParentClassLoader
 */
class ProxyTestEngine : TestEngine {

    private lateinit var containedTestRunner: ContainedRobolectricTestRunner
    private lateinit var delegateClass: Class<*> // SpockEngine Class (in other class loader)
    private lateinit var delegate: Any // SpockEngine (in other class loader)

    override fun getId(): String = "electric-spock"

    override fun discover(discoveryRequest: EngineDiscoveryRequest, uniqueId: UniqueId): TestDescriptor {
        containedTestRunner = ContainedRobolectricTestRunner()

        // Hack - get a security-protected field.
        // Avoiding the error of duplicate service class paths.
        // see: ExtensionClassesLoader#loadClasses()
        findSecurityProtectedField(ClassLoader::class.java, "parent")!!
            .apply { isAccessible = true }
            .set(
                containedTestRunner.sdkSandbox.robolectricClassLoader,
                ProxyParentClassLoader(containedTestRunner.sdkSandbox.robolectricClassLoader.parent)
            )

        // Replacing the ClassLoader of the test class for proper resource loading.
        @Suppress("UNCHECKED_CAST")
        val selectors = discoveryRequest.javaClass
            .getDeclaredField("selectors")
            .apply { isAccessible = true }
            .get(discoveryRequest) as List<DiscoverySelector>
        selectors.map { selector ->
            if (selector is ClassSelector) {
                selector.javaClass // to invoke internal code
                (selector as Any).javaClass
                    .getDeclaredField("javaClass")
                    .apply { isAccessible = true }
                    .set(
                        selector,
                        containedTestRunner.sdkSandbox.bootstrappedClass<Any>(selector.javaClass)
                    )
            }
        }

        delegateClass = containedTestRunner.sdkSandbox
            .bootstrappedClass<Any>(SpockEngine::class.java)
        delegate = delegateClass
            .getConstructor()
            .newInstance()

        return delegateClass
            .getMethod("discover", EngineDiscoveryRequest::class.java, UniqueId::class.java)
            .invoke(delegate, discoveryRequest, uniqueId)
            as TestDescriptor
    }

    override fun execute(request: ExecutionRequest) {
        Thread.currentThread().contextClassLoader = containedTestRunner.sdkSandbox.robolectricClassLoader
        containedTestRunner.containedBeforeTest()

        delegateClass
            .getMethod("execute", ExecutionRequest::class.java)
            .invoke(delegate, request)

        containedTestRunner.containedAfterTest()
        Thread.currentThread().contextClassLoader = ProxyTestEngine::class.java.classLoader
    }
}