package ru.cusxy.sample.app

import android.content.Intent
import hkhc.electricspock.ElectricSpecification
import org.junit.Assert
import org.junit.Test
import org.junit.platform.runner.JUnitPlatform
import org.junit.runner.RunWith
import org.junit.runners.Suite
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows
import org.robolectric.android.controller.ActivityController
import spock.lang.Specification

//@RunWith(RobolectricTestRunner)
//@RunWith(JUnitPlatform)
class WelcomeActivityTest extends ElectricSpecification {

    @Test
    def "maximum of two numbers"() {
        expect:
        Math.max(a, b) == c

        where:
        a << [3, 5, 9]
        b << [7, 4, 9]
        c << [7, 5, 9]
    }

//    @Test
//    void "clicking login should start LoginActivity"() {
//        given:
//        def a = 3
//        def b = 4
//
//        when:
//        def c = a + b
//
//        then:
//        assert c == 7

//        try (ActivityController<WelcomeActivity> controller = Robolectric.buildActivity(WelcomeActivity)) {
//            controller.setup()
//            WelcomeActivity activity = controller.get()
//
//            activity.findViewById(R.id.login).performClick()
//            Intent expectedIntent = new Intent(activity, LoginActivity)
//            Intent actual = Shadows.shadowOf(RuntimeEnvironment.application).getNextStartedActivity()
//            Assert.assertEquals(expectedIntent.getComponent(), actual.getComponent())
//        }
//    }
}