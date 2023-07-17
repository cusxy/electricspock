package ru.cusxy.sample.app.proxy

import org.junit.platform.engine.DiscoverySelector
import org.junit.platform.engine.EngineDiscoveryRequest
import org.junit.platform.engine.ExecutionRequest
import org.junit.platform.engine.TestDescriptor
import org.junit.platform.engine.TestEngine
import org.junit.platform.engine.UniqueId
import org.junit.platform.engine.discovery.ClassSelector
import org.spockframework.runtime.ExtensionClassesLoader
import org.spockframework.runtime.SpockEngine
import ru.cusxy.sample.app.extensions.ContainedRobolectricTestRunner
import java.io.InputStream
import java.lang.reflect.Field
import java.net.URL
import java.util.Enumeration
import java.util.logging.Logger

class ProxyTestEngine : TestEngine {

    private lateinit var containedTestRunner: ContainedRobolectricTestRunner
    private lateinit var delegateClass: Class<*>
    private lateinit var delegate: Any // SpockEngine (in other class loader)

    override fun getId(): String = "electric-spock"

    override fun discover(discoveryRequest: EngineDiscoveryRequest, uniqueId: UniqueId): TestDescriptor {
        containedTestRunner = ContainedRobolectricTestRunner()

        val getDeclaredFields0 = Class::class.java.getDeclaredMethod("getDeclaredFields0", Boolean::class.java)
        getDeclaredFields0.isAccessible = true

        val fields = getDeclaredFields0.invoke(ClassLoader::class.java, false) as Array<Field>
        val parentField = fields.find { field -> field.name == "parent" }

        parentField!!
            .apply { isAccessible = true }
            .set(
                containedTestRunner.containedAndroidSandbox.robolectricClassLoader,
                ProxyClassLoader(containedTestRunner.containedAndroidSandbox.robolectricClassLoader.parent)
            )

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
        delegate = delegateClass
            .getConstructor()
            .newInstance()

        return delegateClass
            .getMethod("discover", EngineDiscoveryRequest::class.java, UniqueId::class.java)
            .invoke(delegate, discoveryRequest, uniqueId)
            as TestDescriptor
    }

    override fun execute(request: ExecutionRequest) {
        Thread.currentThread().contextClassLoader = containedTestRunner.containedAndroidSandbox.robolectricClassLoader
        containedTestRunner.containedBeforeTest()

        delegateClass
            .getMethod("execute", ExecutionRequest::class.java)
            .invoke(delegate, request)

        containedTestRunner.containedAfterTest()
        Thread.currentThread().contextClassLoader = ProxyClassLoader::class.java.classLoader
    }

    private class ProxyClassLoader(private val delegate: ClassLoader) : ClassLoader(delegate) {

        override fun equals(other: Any?): Boolean {
            return delegate.equals(other)
        }

        override fun hashCode(): Int {
            return delegate.hashCode()
        }

        override fun toString(): String {
            return delegate.toString()
        }

        override fun loadClass(name: String?): Class<*> {
            return delegate.loadClass(name)
        }

        override fun getResource(name: String?): URL? {
            return delegate.getResource(name)
        }

        override fun getResources(name: String?): Enumeration<URL> {
            return when (name) {
                ExtensionClassesLoader.EXTENSION_DESCRIPTOR_PATH,
                ExtensionClassesLoader.CONFIG_DESCRIPTOR_PATH -> findResources(name)

                else -> delegate.getResources(name)
            }
        }

        override fun getResourceAsStream(name: String?): InputStream {
            return delegate.getResourceAsStream(name)
        }

        override fun setDefaultAssertionStatus(enabled: Boolean) {
            delegate.setDefaultAssertionStatus(enabled)
        }

        override fun setPackageAssertionStatus(packageName: String?, enabled: Boolean) {
            delegate.setPackageAssertionStatus(packageName, enabled)
        }

        override fun setClassAssertionStatus(className: String?, enabled: Boolean) {
            delegate.setClassAssertionStatus(className, enabled)
        }

        override fun clearAssertionStatus() {
            delegate.clearAssertionStatus()
        }
    }
}