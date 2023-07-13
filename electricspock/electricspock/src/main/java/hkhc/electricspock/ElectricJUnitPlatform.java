/*
 * Copyright 2016 Herman Cheung
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package hkhc.electricspock;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.manipulation.Filter;
import org.junit.runner.manipulation.Filterable;
import org.junit.runner.manipulation.NoTestsRemainException;
import org.junit.runner.manipulation.Sortable;
import org.junit.runner.manipulation.Sorter;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.InitializationError;
import org.robolectric.internal.AndroidSandbox;
import org.spockframework.runtime.model.SpecInfo;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.SecureRandom;

import hkhc.electricspock.internal.ContainedRobolectricTestRunner;
import hkhc.electricspock.internal.ElectricSpockInterceptor;
import spock.lang.Specification;
import spock.lang.Title;

/**
 * Created by herman on 27/12/2016.
 * Test Runner
 */

public class ElectricJUnitPlatform extends Runner implements Filterable, Sortable {

    private AndroidSandbox androidSandbox;

    /* it is used to setup Robolectric infrastructure, and not used to run actual test cases */
    private ContainedRobolectricTestRunner containedRunner;

    /* Used to check if object of proper class is obtained from getSpec method */
    private Class specInfoClass;

    /* the real test runner to run test classes. It is enclosed by ElectricJUnitPlatform so that it is
    run within Robolectric interception
     */
    private Runner junitPlatform;

    static {
        new SecureRandom(); // this starts up the Poller SunPKCS11-Darwin thread early, outside of any Robolectric classloader
    }

    public ElectricJUnitPlatform(Class<? extends Specification> specClass) throws InitializationError {

        /* The project is so sensitive to the version of Robolectric, that we strictly check
        its version before proceed
         */
        // (new RobolectricVersionChecker()).checkRobolectricVersion();

        if (!(specClass.getClassLoader() instanceof AndroidSandbox.SdkSandboxClassLoader)) {
            containedRunner = new ContainedRobolectricTestRunner(specClass);
            androidSandbox = containedRunner.getContainedAndroidSandbox();
            specInfoClass = androidSandbox.bootstrappedClass(SpecInfo.class);

            // Since we have bootstrappedClass we may properly initialize
            junitPlatform = createJUnitPlatform(specClass);

            registerSpec(specClass);
        } else {
            junitPlatform = new Runner() {
                @Override
                public Description getDescription() {
                    return Description.EMPTY;
                }

                @Override
                public void run(RunNotifier notifier) {

                }
            };
        }

    }

    /**
     * JUnitPlatform is the test runner for Spock specification. This method Load the spec class and
     * JUnitPlatform class with Robolectric sandbox, so that Robolectric can intercept the Android API
     * code. That's how we bridge Spock framework and Robolectric together.
     *
     * @param specClass the Specification class to be run under JUnitPlatform
     */
    private Runner createJUnitPlatform(Class<? extends Specification> specClass) {

        Class bootstrappedTestClass = androidSandbox.bootstrappedClass(specClass);

        try {

            return (Runner) androidSandbox
                    .bootstrappedClass(JUnitPlatform.class)
                    .getConstructor(Class.class)
                    .newInstance(bootstrappedTestClass);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * Register an interceptor to specInfo of every method in specification.
     */
    private void registerSpec(Class<? extends Specification> specClass) {

        TestRunnerStorage.INSTANCE.storage.put(specClass, containedRunner);

//        Constructor interceptorConstructor = getInterceptorConstructor();
//
//        for (Method method : junitPlatform.getClass().getDeclaredMethods()) {
//            Object specInfo = getSpec(method);
//            if (specInfo != null) {
//                try {
//                    // ElectricSpockInterceptor register itself to SpecInfo on construction,
//                    // no need to keep a ref here
//                    interceptorConstructor.newInstance(specInfo, containedRunner);
//                } catch (IllegalAccessException e) {
//                    throw new RuntimeException(e);
//                } catch (InvocationTargetException e) {
//                    throw new RuntimeException(e);
//                } catch (InstantiationException e) {
//                    throw new RuntimeException(e);
//                }
//            }
//        }

    }

    /**
     * Get the SpecInfo from Specification. However, the SpecInfo instance it return will be under
     * Robolectric sandbox, so it cannot be casted directly to SpecInfo statically.
     *
     * @param method the getSpec method
     * @return the SpecInfo object loaded under Robolectric sandbox
     */
    private Object getSpec(Method method) {

        if (method.getName().equals("getSpec")) {
            method.setAccessible(true);
            try {
                Object specInfo = method.invoke(junitPlatform);
                if (specInfo.getClass() != specInfoClass) {
                    throw new RuntimeException("Failed to obtain SpecInfo instance from getSpec method. Instance of '"
                            + specInfo.getClass().getName() + "' is obtained");
                }
                return specInfo;
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        } else {
            return null;
        }
    }

    /**
     * Get a sandboxed constructor of interceptor
     *
     * @return
     */
    private Constructor getInterceptorConstructor() {

        try {
            return androidSandbox
                    .bootstrappedClass(ElectricSpockInterceptor.class)
                    .getConstructor(
                            specInfoClass,
                            ContainedRobolectricTestRunner.class
                    );
        } catch (NoSuchMethodException e) {
            // it should not happen in production code as the class
            // ElectricSpockInterceptor is known
            throw new RuntimeException(e);
        }

    }


    public Description getDescription() {

        Description originalDesc = junitPlatform.getDescription();

        Class<?> testClass = originalDesc.getTestClass();
        if (testClass == null) {
            return originalDesc;
            // throw new RuntimeException("Unexpected null testClass");
        }

        String title = null;
        Annotation[] annotations = testClass.getAnnotations();
        for (Annotation a : annotations) {
            if (a instanceof Title) {
                title = ((Title) a).value();
                break;
            }
        }

        Description overridedDesc = Description.createSuiteDescription(
                title == null ? testClass.getName() : title
        );
        for (Description d : originalDesc.getChildren()) {
            overridedDesc.addChild(d);
        }

        return overridedDesc;

    }

    public void run(RunNotifier notifier) {
        junitPlatform.run(notifier);
    }

    public void filter(Filter filter) throws NoTestsRemainException {
        ((Filterable) junitPlatform).filter(filter);
    }

    public void sort(Sorter sorter) {
        ((Sortable) junitPlatform).sort(sorter);
    }

}