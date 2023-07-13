package hkhc.electricspock

import org.junit.runner.Description
import org.junit.runner.Runner
import org.junit.runner.notification.RunNotifier

class MySputnik(
    private val clazz: Class<*>
) : Runner() {
    override fun getDescription(): Description {
        TODO("Not yet implemented")
    }

    override fun run(notifier: RunNotifier?) {
        TODO("Not yet implemented")
    }

    //    private var extensionsRun: Boolean = false
//    private var spec: SpecInfo? = null
//    private var description: Description? = null
//
//    init {
//        try {
//            VersionChecker().checkGroovyVersion("JUnit runner")
//        } catch (e: IncompatibleGroovyVersionException) {
//            throw InitializationError(e)
//        }
//    }
//
//    override fun getDescription(): Description {
//        runExtensionsIfNecessary()
//        return description!!
//    }
//
//    override fun run(notifier: RunNotifier) {
//        runExtensionsIfNecessary()
//        getSpec().addListener(
//            object : IRunListener {
//                private val iteration: IterationInfo? = null
//                private val feature: FeatureInfo? = null
//
//                override fun beforeSpec(spec: SpecInfo) {
//                }
//                override fun beforeFeature(feature: FeatureInfo) {
//                    notifier.fireTestStarted(JUnitDescriptionGenerator.describeFeature(feature, spec))
//                }
//                override fun beforeIteration(iteration: IterationInfo) {
//                    notifier.fireTestStarted(JUnitDescriptionGenerator.describeIteration(iteration, spec))
//                }
//                override fun afterIteration(iteration: IterationInfo) {
//
//                }
//                override fun afterFeature(feature: FeatureInfo) {
//                    notifier.fireTestFinished(JUnitDescriptionGenerator.describeFeature(feature, spec))
//                }
//                override fun afterSpec(spec: SpecInfo) {}
//                override fun error(error: ErrorInfo) {}
//                override fun specSkipped(spec: SpecInfo) {}
//                override fun featureSkipped(feature: FeatureInfo) {}
//            }
//        )
//        RunContext.get().createSpecRunner(getSpec()).runSpec()
//    }
//
//    private fun runExtensionsIfNecessary() {
//        if (extensionsRun) return
//        RunContext.get().createExtensionRunner(getSpec()).run()
//        extensionsRun = true
//    }
//
//    private fun getSpec(): SpecInfo {
//        if (spec == null) {
//            spec = SpecInfoBuilder(clazz).build()
//            description = JUnitDescriptionGenerator.describeSpec(spec)
//        }
//        return spec!!
//    }
}