package hkhc.electricspock;

import java.util.HashMap;

import hkhc.electricspock.internal.ContainedRobolectricTestRunner;
import spock.lang.Specification;

public class TestRunnerStorage {

    public HashMap<Class<? extends Specification>, ContainedRobolectricTestRunner> storage = new HashMap<>();

    public static final TestRunnerStorage INSTANCE;

    private TestRunnerStorage() {}

    static {
        INSTANCE = new TestRunnerStorage();
    }
}
