package ru.cusxy.sample.app

import android.content.Intent
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows
import org.robolectric.android.controller.ActivityController
import spock.lang.Specification

@RunWith(RobolectricTestRunner)
class WelcomeActivityTest extends Specification {

    @Test
    void "clicking login should start LoginActivity"() {
        try (ActivityController<WelcomeActivity> controller = Robolectric.buildActivity(WelcomeActivity)) {
            controller.setup()
            WelcomeActivity activity = controller.get()

            activity.findViewById(R.id.login).performClick()
            Intent expectedIntent = new Intent(activity, LoginActivity)
            Intent actual = Shadows.shadowOf(RuntimeEnvironment.application).getNextStartedActivity()
            Assert.assertEquals(expectedIntent.getComponent(), actual.getComponent())
        }
    }
}