package net.jqwik.engine.hooks;

import org.junit.platform.commons.support.*;

import net.jqwik.api.*;
import net.jqwik.api.lifecycle.*;

public class DisabledHook implements SkipExecutionHook {

	@Override
	public SkipResult shouldBeSkipped(LifecycleContext context) {
		return context.annotatedElement()
					  .flatMap(element -> AnnotationSupport.findAnnotation(element, Disabled.class))
					  .map(disabled -> {
						  String reason = disabled.value().isEmpty() ?
											  String.format("@Disabled: %s", context.annotatedElement().get())
											  : disabled.value();
						  return SkipResult.skip(reason);
					  })
					  .orElse(SkipResult.doNotSkip());
	}

	@Override
	public int skipExecutionOrder() {
		return Hooks.SkipExecution.DISABLED_ORDER;
	}

	@Override
	public boolean applyToDescendants() {
		return true;
	}

}
