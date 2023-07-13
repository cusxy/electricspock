package hkhc.electricspock.internal

import hkhc.electricspock.ElectricSputnik
import org.spockframework.runtime.extension.AbstractMethodInterceptor
import org.spockframework.runtime.extension.IMethodInvocation
import org.spockframework.runtime.model.SpecInfo

class ElectricSpockInterceptor(
    spec: SpecInfo,
    private val containedTestRunner: ContainedRobolectricTestRunner
) : AbstractMethodInterceptor() {

    init {
        spec.addInterceptor(this)
    }

    /**
     * Migrate from RobolectricTestRunner.methodBlock
     * Replace the classloader by Robolectric's when executing a specification. Restore it when
     * execution finished.
     *
     * @param invocation The method invocation to be intercept
     */
    override fun interceptSpecExecution(invocation: IMethodInvocation) {
        Thread.currentThread().contextClassLoader =
            containedTestRunner.getContainedAndroidSandbox().robolectricClassLoader

        containedTestRunner.containedBeforeTest()
        try {
            invocation.proceed()
        } finally {
            try {
                containedTestRunner.containedAfterTest()
            } finally {
                Thread.currentThread().contextClassLoader =
                    ElectricSputnik::class.java.classLoader
            }
        }
    }
}