package hkhc.electricspock;

import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.manipulation.Filter;
import org.junit.runner.manipulation.Filterable;
import org.junit.runner.notification.RunNotifier;

import spock.lang.Specification;

public class ElectricSputnikBridge extends Runner implements Filterable {

    private final ElectricSputnikV2 delegate;

    public ElectricSputnikBridge(Class<? extends Specification> testClass) {
        delegate = new ElectricSputnikV2(testClass);
    }

    @Override
    public Description getDescription() {
        return delegate.getDescription();
    }

    @Override
    public void run(RunNotifier notifier) {
        delegate.run(notifier);
    }

    @Override
    public void filter(Filter filter) {
        delegate.filter(filter);
    }
}
