package ru.cusxy.sample.app.extensions

import org.spockframework.runtime.extension.IGlobalExtension
import org.spockframework.runtime.model.SpecInfo
import java.util.logging.Logger

class ElectricSpockExtension : IGlobalExtension {

    private val logger = Logger.getLogger(ElectricSpockExtension::class.java.name)

    private lateinit var containedTestRunner: ContainedRobolectricTestRunner

    override fun start() {
        logger.info("Start")
    }

    override fun visitSpec(spec: SpecInfo) {
        logger.info("Visit spec ${spec.name}")

        containedTestRunner = ContainedRobolectricTestRunner()
        spec.reflection = containedTestRunner.containedAndroidSandbox.bootstrappedClass<Any>(spec.reflection)
//        val interceptor = containedTestRunner.containedAndroidSandbox
//            .bootstrappedClass<Any>(ElectricSpockInterceptor::class.java)
//            .getConstructor(ContainedRobolectricTestRunner::class.java)
//            .newInstance(containedTestRunner) as ElectricSpockInterceptor
        val interceptor = ElectricSpockInterceptor(containedTestRunner)

        spec.addInterceptor(interceptor)
    }

    override fun stop() {
        logger.info("Stop")
    }
}