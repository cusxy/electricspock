package hkhc.electricspock

import hkhc.electricspock.internal.ContainedRobolectricTestRunner
import org.junit.Rule
import org.junit.rules.ExternalResource
import org.junit.runner.RunWith
import spock.lang.Specification

@RunWith(ElectricJUnitPlatform.class)
class ElectricSpecification extends Specification {

    @Rule
    ExternalResource switchClassLoader = new ExternalResource() {

        ContainedRobolectricTestRunner runner = TestRunnerStorage.INSTANCE.storage.get(ElectricSpecification.this.getClass())

        @Override
        protected void before() throws Throwable {
            println "!@# BEFORE"
            Thread.currentThread().setContextClassLoader(runner.getContainedAndroidSandbox().getRobolectricClassLoader())
            runner.containedBeforeTest()
        }

        @Override
        protected void after() {
            println "!@# AFTER"
            runner.containedAfterTest()
            Thread.currentThread().setContextClassLoader(ElectricJUnitPlatform.class.getClassLoader())
        }
    }
}