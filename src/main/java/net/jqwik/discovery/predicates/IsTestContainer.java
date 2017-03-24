package net.jqwik.discovery.predicates;

import java.lang.reflect.*;
import java.util.function.*;

import org.junit.platform.commons.support.*;

import net.jqwik.discovery.specs.*;
import net.jqwik.support.*;

public class IsTestContainer implements Predicate<Class<?>> {

	private static final ExampleDiscoverySpec exampleSpec = new ExampleDiscoverySpec();
	private static final PropertyDiscoverySpec propertySpec = new PropertyDiscoverySpec();

	private static final Predicate<Class<?>> isPotentialTestContainer = new IsPotentialTestContainer();
	private static final Predicate<Class<?>> isGroup = new IsContainerAGroup();

	@Override
	public boolean test(Class<?> candidate) {
		if (!isPotentialTestContainer.test(candidate)) {
			return false;
		}
		return hasTests(candidate) || hasGroups(candidate);
	}

	private boolean hasTests(Class<?> candidate) {
		Predicate<Method> hasATestMethod = method -> exampleSpec.shouldBeDiscovered(method) || propertySpec.shouldBeDiscovered(method);
		return !ReflectionSupport.findMethods(candidate, hasATestMethod, HierarchyTraversalMode.TOP_DOWN).isEmpty();
	}

	private boolean hasGroups(Class<?> candidate) {
		return !JqwikReflectionSupport.findNestedClasses(candidate, isGroup).isEmpty();
	}

}
