package hkhc.electricspock.internal;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.internal.AndroidSandbox;
import org.robolectric.internal.bytecode.InstrumentationConfiguration;

import java.lang.reflect.Method;
import java.util.List;

import spock.lang.Specification;

/**
 * Modified RobolectricTestRunner solely to be used by Spock interceptor.
 */

public class ContainedRobolectricTestRunner extends RobolectricTestRunner {

    private Class<? extends Specification> specClass = null;
    private FrameworkMethod placeholderMethod = null;
    private AndroidSandbox androidSandbox = null;
    private Method bootstrapedMethod = null;

    /*
    A place holder test class to obtain a proper FrameworkMethod (which is actually a
    RoboFrameworkTestMethod) by reusing existing code in RobolectricTestRunner
     */
    public static class PlaceholderTest {
        /* Just a placeholder, the actual content of the test method is not important */
        @Test
        public void testPlaceholder() {

        }
    }

    /*
    Pretend to be a test runner for the placeholder test class. We don't actually run that test
    method. Just use it to trigger all initialization of Robolectric infrastructure, and use it
    to run Spock specification.
     */
    public ContainedRobolectricTestRunner(Class<? extends Specification> specClass) throws InitializationError {
        super(PlaceholderTest.class);
        this.specClass = specClass;
    }

    FrameworkMethod getPlaceHolderMethod() {

        if (placeholderMethod==null) {
            List<FrameworkMethod> childs = getChildren();
            placeholderMethod = childs.get(0);
        }

        return placeholderMethod;

    }

    @Override
    protected List<FrameworkMethod> getChildren() {
        return super.getChildren();
    }

    private Method getBootstrapedMethod() {

        if (bootstrapedMethod==null) {
            bootstrapedMethod = createBootstrapedMethod();
        }
        return bootstrapedMethod;

    }

    private Method getMethod(Class<?> clazz, String methodName) {
        try {
            return clazz.getMethod(methodName);
        }
        catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    Method createBootstrapedMethod() {

        FrameworkMethod placeholderMethod = getPlaceHolderMethod();
        AndroidSandbox androidSandbox = getContainedAndroidSandbox();

        // getTestClass().getJavaClass() should always be PlaceholderTest.class,
        // load under Robolectric's class loader
        Class bootstrappedTestClass = androidSandbox.bootstrappedClass(
                getTestClass().getJavaClass());

        return getMethod(bootstrappedTestClass, placeholderMethod.getMethod().getName());

    }

    /*
    Override to add itself to doNotAcquireClass, so as to avoid classloader conflict
     */
    @Override
    @NotNull
    protected InstrumentationConfiguration createClassLoaderConfig(final FrameworkMethod method) {

        return new InstrumentationConfiguration.Builder(super.createClassLoaderConfig(method))
                .doNotAcquireClass(getClass())
                .build();

    }

    public AndroidSandbox getContainedAndroidSandbox() {

        if (androidSandbox ==null) {
            androidSandbox = getSandbox(getPlaceHolderMethod());
            configureSandbox(androidSandbox, getPlaceHolderMethod());
        }

        return androidSandbox;

    }

    public void containedBeforeTest() throws Throwable {
        super.beforeTest(getContainedAndroidSandbox(), getPlaceHolderMethod(), getBootstrapedMethod());
    }

    public void containedAfterTest() {
        super.afterTest(getPlaceHolderMethod(), getBootstrapedMethod());
    }

    /**
     * Get @Config declaration from class or method declaration.
     * If @Config is not available at method declaration, we delegate the task to the original
     * RobolectricTestRunner. We build our own config from @Config annotation at method declaration
     * @param method Find the @Config annotation at method
     * @return The Config object it found
     */
    @Override
    public Config getConfig(Method method) {
        Config baseConfig = super.getConfig(method);
        Config config = specClass.getAnnotation(Config.class);
        if (config==null) {
            return baseConfig;
        }
        else {
            return new Config.Builder(baseConfig).overlay(config).build();
        }

    }



}