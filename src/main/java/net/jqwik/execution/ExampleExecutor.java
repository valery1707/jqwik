package net.jqwik.execution;

import org.junit.platform.engine.*;

import net.jqwik.api.*;
import net.jqwik.descriptor.*;
import net.jqwik.support.*;

public class ExampleExecutor extends AbstractMethodExecutor<ExampleMethodDescriptor, ExampleLifecycle> {

	@Override
	protected TestExecutionResult execute(ExampleMethodDescriptor methodDescriptor, Object testInstance) {
		SafeExecutor.Executable executable = () -> JqwikReflectionSupport.invokeMethod(methodDescriptor.getTargetMethod(), testInstance);
		return new SafeExecutor().executeSafely(executable);
	}

}
