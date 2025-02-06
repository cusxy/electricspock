# ElectricSpock and Groovy Android Gradle Plugin: Testing Android with Spock and Robolectric

This project shows how to use the **Spock** testing framework and **Robolectric** together for testing Android applications. It also includes a custom **Groovy Android Gradle Plugin** to make Groovy work in modern Android projects.

**Note:** This project was inspired by [hkhc/electricspock](https://github.com/hkhc/electricspock) and [groovy/groovy-android-gradle-plugin](https://github.com/groovy/groovy-android-gradle-plugin) which are already deprecated.

## What are Spock and Robolectric?

*   **Spock:** A testing framework for Groovy and Java. It makes your tests easy to read and write. It uses a clear structure: `given:`, `when:`, `then:`.
*   **Robolectric:** A library that lets you run Android tests on your computer (JVM) without an emulator or real device. It simulates the Android environment.

## What is ElectricSpock?

**ElectricSpock** is a library that helps you use Spock and Robolectric together. Normally, they don't work well together because they use different test runners. ElectricSpock solves this problem.

## What is the Groovy Android Gradle Plugin?

The **Groovy Android Gradle Plugin** is a custom plugin that allows you to use Groovy in your Android projects. Apache no longer supports Groovy for Android, but this plugin brings it back!

## Why Use This Combination?

*   **Readable Tests:** Spock makes your tests very easy to read and understand.
*   **Fast Tests:** Robolectric makes your tests run very fast because you don't need an emulator.
*   **Groovy:** If you like Groovy, this lets you use it in your Android projects.

## How to Use It: A Step-by-Step Guide

Here's how to use ElectricSpock and the Groovy Android Gradle Plugin in your Android project:

### Step 1: Apply the Groovy Android Gradle Plugin

First, you need to apply the `ru.cusxy.mgga.groovy-android` plugin to your module's `build.gradle.kts` file. This plugin will compile your Groovy code.

```kotlin
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("ru.cusxy.mgga.groovy-android") // Apply the Groovy Android plugin
}
```

### Step 2: Add the ElectricSpock Dependency

Next, add the `electricspock` dependency to your module's `build.gradle.kts` file. This will add ElectricSpock to your project.

```kotlin
dependencies { 
    testImplementation("ru.cusxy.mgga:electricspock:1.0.0") // Add ElectricSpock
    testImplementation("org.spockframework:spock-core:2.3-groovy-3.0") // Add Spock
    testImplementation("org.robolectric:robolectric:4.14.1" ) // Add Robolectric
    testImplementation("org.codehaus.groovy:groovy-all:3.0.22") // Add Groovy
}
```

### Step 3: Exclude Spock Engine

You need to exclude the default Spock engine from the JUnit Platform. Add this to your `build.gradle.kts` file:

```kotlin
android {
    testOptions {
        unitTests {
            all { test ->
                test.useJUnitPlatform {
                    excludeEngines("spock") // Exclude the default Spock engine
                }
                test.jvmArgs( // Required for advanced reflection 
                    "--add-opens", "java.base/java.lang=ALL-UNNAMED",
                )
            }
        }
    }
}
```

### Step 4: Create a Groovy Test File

Now, create a Groovy test file in your `src/test/groovy` directory. For example, [WelcomeActivityTest.groovy](sample/src/test/groovy/ru/cusxy/mgga/app/WelcomeActivityTest.groovy).

```groovy
import android.content.Intent
import org.robolectric.RuntimeEnvironment
import spock.lang.Specification
import org.robolectric.Robolectric
import org.robolectric.Shadows
import org.robolectric.android.controller.ActivityController

class WelcomeActivityTest extends Specification {
    
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
```

### Step 5: Run Your Tests

You can now run your tests from Android Studio or using the Gradle command:

```bash
./gradlew :sample:testDebugUnitTest
```

## How It Works: A Simple Explanation

1.  **Groovy Code:** You write your tests in Groovy using Spock's syntax.
2.  **Groovy Plugin:** The `ru.cusxy.mgga.groovy-android` plugin compiles your Groovy code into Java bytecode.
3.  **ElectricSpock:** ElectricSpock is a custom `TestEngine` for JUnit Platform. It uses a special trick to load the test classes with Robolectric's class loader.
4.  **Robolectric:** Robolectric simulates the Android environment.
5.  **Spock:** Spock structures your tests and makes them easy to read.
6. **JUnit Platform:** JUnit Platform runs your tests.

## Important Notes

*   **Groovy is not officially supported by Apache for Android.** This project uses a custom plugin to make it work.
*   **This setup is complex.** It's not the standard way to test Android apps.
*   **Kotlin is the recommended language for Android.** Consider using Kotlin and a modern testing framework like Kotest for new projects.
* **ElectricSpock is a complex solution.** It is not a standard way to integrate Spock and Robolectric.
* **This project is for educational purposes.** _(well almost)_ It shows how to make Spock and Robolectric work together.

## License

This project is licensed under the [Apache 2.0](LICENSE).
