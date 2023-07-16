package ru.cusxy.sample.app.proxy

import org.junit.platform.engine.DiscoverySelector
import org.junit.platform.engine.EngineDiscoveryRequest
import org.junit.platform.engine.ExecutionRequest
import org.junit.platform.engine.TestDescriptor
import org.junit.platform.engine.TestEngine
import org.junit.platform.engine.UniqueId
import org.junit.platform.engine.discovery.ClassSelector
import org.spockframework.runtime.SpockEngine
import ru.cusxy.sample.app.extensions.ContainedRobolectricTestRunner

class ProxyTestEngine : TestEngine {

    private lateinit var containedTestRunner: ContainedRobolectricTestRunner
    private lateinit var delegateClass: Class<*>
    private lateinit var delegate: Any // SpockEngine (in other class loader)

    override fun getId(): String = "electric-spock"

    override fun discover(discoveryRequest: EngineDiscoveryRequest, uniqueId: UniqueId): TestDescriptor {
        containedTestRunner = ContainedRobolectricTestRunner()

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
                        containedTestRunner.containedAndroidSandbox.bootstrappedClass<Any>(selector.javaClass)
                    )
            }
        }

        delegateClass = containedTestRunner.containedAndroidSandbox
            .bootstrappedClass<Any>(SpockEngine::class.java)
//        delegateClass = SpockEngine::class.java
        delegate = delegateClass
            .getConstructor()
            .newInstance()

        return delegateClass
            .getMethod("discover", EngineDiscoveryRequest::class.java, UniqueId::class.java)
            .invoke(delegate, discoveryRequest, uniqueId)
            as TestDescriptor
    }

    override fun execute(request: ExecutionRequest) {
        containedTestRunner.containedBeforeTest()

        delegateClass
            .getMethod("execute", ExecutionRequest::class.java)
            .invoke(delegate, request)

        containedTestRunner.containedAfterTest()
    }
}