package net.jqwik.support;

import org.junit.platform.commons.util.*;

public class JqwikStringSupport {
	public static String nullSafeToString(Class<?>... classes) {
		return StringUtils.nullSafeToString(classes);
	}

}
