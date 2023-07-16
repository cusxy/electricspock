package ru.cusxy.sample.app.extensions

import org.spockframework.runtime.extension.AbstractMethodInterceptor
import org.spockframework.runtime.extension.IMethodInvocation
import java.util.logging.Logger

class ElectricSpockInterceptor(
    private val containedTestRunner: ContainedRobolectricTestRunner,
) : AbstractMethodInterceptor() {

    private val logger = Logger.getLogger(ElectricSpockInterceptor::class.java.name)

    /**
     * Migrate from RobolectricTestRunner.methodBlock
     * Replace the classloader by Robolectric's when execution a specification. Restore it when execution finished.
     */
    override fun interceptSpecExecution(invocation: IMethodInvocation) {
        logger.info("Before method ${invocation.method.name}")
        Thread.currentThread().contextClassLoader = containedTestRunner.containedAndroidSandbox.robolectricClassLoader
        containedTestRunner.containedBeforeTest()

        // todo: this try/finally probably isn't right -- should mimic RunAfters? [xw]
        try {
            invocation.proceed()
        } finally {
            logger.info("After method ${invocation.method.name}")
            containedTestRunner.containedAfterTest()
            Thread.currentThread().contextClassLoader = ElectricSpockExtension::class.java.classLoader
        }
    }
}