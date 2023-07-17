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
 * Прокси для SpockEngine подменяет загрузчик классов тестового класса для возможности загрузки классов Robolectric.
 *
 * Для корректной работы движка требуется исключить spock engine из загрузчика JUnitPlatform:
 *
 * ```groovy
 *   tasks.test {
 *     useJUnitPlatform {
 *       excludeEngines 'spock'
 *     }
 *   }
 * ```
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