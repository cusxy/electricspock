package ru.cusxy.mgga.electricspock.internal

import org.spockframework.runtime.ExtensionClassesLoader
import java.io.InputStream
import java.net.URL
import java.util.Enumeration

internal class ProxyParentClassLoader(private val delegate: ClassLoader) : ClassLoader(delegate) {

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
        // required after
        return when (name) {
            ExtensionClassesLoader.EXTENSION_DESCRIPTOR_PATH,
            ExtensionClassesLoader.CONFIG_DESCRIPTOR_PATH -> findResources(name)

            else -> delegate.getResources(name)
        }
    }

    override fun getResourceAsStream(name: String?): InputStream? {
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