package net.jqwik.descriptor;

import net.jqwik.*;
import net.jqwik.api.*;

public class PropertyConfiguration {

	public static PropertyConfiguration from(Property property, PropertyDefaultValues propertyDefaultValues) {
		int tries = property.tries() == Property.TRIES_NOT_SET //
				? propertyDefaultValues.tries()
				: property.tries();
		int maxDiscardRatio = property.maxDiscardRatio() == Property.MAX_DISCARD_RATIO_NOT_SET //
				? propertyDefaultValues.maxDiscardRatio()
				: property.maxDiscardRatio();
		return new PropertyConfiguration(property.stereotype(), property.seed(), tries, maxDiscardRatio, property.shrinking());
	}

	private final String stereotype;
	private final String seed;
	private final int tries;
	private final int maxDiscardRatio;
	private final ShrinkingMode shrinkingMode;

	public PropertyConfiguration(
		String stereotype,
		String seed,
		int tries,
		int maxDiscardRatio,
		ShrinkingMode shrinkingMode
	) {
		this.stereotype = stereotype;
		this.seed = seed;
		this.tries = tries;
		this.maxDiscardRatio = maxDiscardRatio;
		this.shrinkingMode = shrinkingMode;
	}

	public String getSeed() {
		return seed;
	}

	public PropertyConfiguration withSeed(String changedSeed) {
		return new PropertyConfiguration(this.stereotype, changedSeed, this.tries, this.maxDiscardRatio, this.shrinkingMode);
	}

	public String getStereotype() {
		return stereotype;
	}

	public int getTries() {
		return tries;
	}

	public int getMaxDiscardRatio() {
		return maxDiscardRatio;
	}

	public ShrinkingMode getShrinkingMode() {
		return shrinkingMode;
	}

}
