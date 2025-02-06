package ru.cusxy.mgga.app

import android.content.Intent
import org.robolectric.RuntimeEnvironment
import spock.lang.Specification
import org.robolectric.Robolectric
import org.robolectric.Shadows
import org.robolectric.android.controller.ActivityController

class WelcomeActivityTest extends Specification {

    def "maximum of two numbers"() {
        expect:
            Math.max(a, b) == c

        where:
            a << [3, 5, 9]
            b << [7, 4, 9]
            c << [7, 5, 9]
    }

    def "clicking login should start LoginActivity"() {
        given:
            ActivityController<WelcomeActivity> controller = Robolectric.buildActivity(WelcomeActivity)
            controller.setup()
            def activity = controller.get()

        and:
            def expectIntent = new Intent(activity, LoginActivity)

        when:
            activity.findViewById(R.id.login).performClick()
            def actualIntent = Shadows.shadowOf(RuntimeEnvironment.application).getNextStartedActivity()

        then:
            expectIntent.component == actualIntent.component
    }
}