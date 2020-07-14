package net.jqwik.engine.properties.shrinking;

import java.util.*;
import java.util.concurrent.atomic.*;
import java.util.function.*;
import java.util.stream.*;

import net.jqwik.api.*;
import net.jqwik.engine.properties.*;
import net.jqwik.engine.properties.shrinking.ShrinkableTypesForTest.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import static net.jqwik.api.ShrinkingTestHelper.*;

@Group
@Label("ShrinkableString")
public class ShrinkableStringTests {

	private final AtomicInteger counter = new AtomicInteger(0);
	private final Runnable count = counter::incrementAndGet;

	@SuppressWarnings("unchecked")
	private final Consumer<String> valueReporter = mock(Consumer.class);
	private final Consumer<FalsificationResult<String>> reporter = result -> valueReporter.accept(result.value());

	@Example
	void creation() {
		Shrinkable<String> shrinkable = createShrinkableString("abcd", 0);
		assertThat(shrinkable.distance()).isEqualTo(ShrinkingDistance.of(4, 6));
		assertThat(shrinkable.value()).isEqualTo("abcd");
	}

	@Example
	@Label("report all falsified on the way")
	void reportFalsified() {
		Shrinkable<String> shrinkable = createShrinkableString("bcd", 0);

		ShrinkingSequence<String> sequence = shrinkable.shrink((TestingFalsifier<String>) String::isEmpty);

		assertThat(sequence.next(count, reporter)).isTrue();
		assertThat(sequence.current().value()).isEqualTo("bc");
		verify(valueReporter).accept("bc");

		assertThat(sequence.next(count, reporter)).isTrue();
		assertThat(sequence.current().value()).isEqualTo("b");
		verify(valueReporter).accept("b");

		assertThat(sequence.next(count, reporter)).isTrue();
		assertThat(sequence.current().value()).isEqualTo("a");
		verify(valueReporter).accept("a");

		assertThat(sequence.next(count, reporter)).isFalse();
		verifyNoMoreInteractions(valueReporter);
	}

	@Group
	class Shrinking {

		@Example
		void downAllTheWay() {
			Shrinkable<String> shrinkable = createShrinkableString("abc", 0);

			ShrinkingSequence<String> sequence = shrinkable.shrink((TestingFalsifier<String>) aString -> false);

			assertThat(sequence.next(count, reporter)).isTrue();
			assertThat(sequence.current().value().length()).isEqualTo(2);
			assertThat(sequence.next(count, reporter)).isTrue();
			assertThat(sequence.current().value().length()).isEqualTo(1);
			assertThat(sequence.next(count, reporter)).isTrue();
			assertThat(sequence.current().value().length()).isEqualTo(0);
			assertThat(sequence.next(count, reporter)).isFalse();

			assertThat(counter.get()).isEqualTo(3);
		}

		@Example
		void downToMinSize() {
			Shrinkable<String> shrinkable = createShrinkableString("aaaaa", 2);

			ShrinkingSequence<String> sequence = shrinkable.shrink((TestingFalsifier<String>) aString -> false);

			assertThat(sequence.next(count, reporter)).isTrue();
			assertThat(sequence.current().value().length()).isEqualTo(4);
			assertThat(sequence.next(count, reporter)).isTrue();
			assertThat(sequence.current().value().length()).isEqualTo(3);
			assertThat(sequence.next(count, reporter)).isTrue();
			assertThat(sequence.current().value().length()).isEqualTo(2);
			assertThat(sequence.next(count, reporter)).isFalse();

			assertThat(counter.get()).isEqualTo(3);
		}

		@Example
		void downToNonEmpty() {
			Shrinkable<String> shrinkable = createShrinkableString("abcd", 0);

			ShrinkingSequence<String> sequence = shrinkable.shrink((TestingFalsifier<String>) String::isEmpty);

			assertThat(sequence.next(count, reporter)).isTrue();
			assertThat(sequence.current().value().length()).isEqualTo(3);
			assertThat(sequence.next(count, reporter)).isTrue();
			assertThat(sequence.current().value().length()).isEqualTo(2);
			assertThat(sequence.next(count, reporter)).isTrue();
			assertThat(sequence.current().value().length()).isEqualTo(1);
			assertThat(sequence.next(count, reporter)).isFalse();

			assertThat(counter.get()).isEqualTo(3);
		}

		@Example
		void alsoShrinkCharacters() {
			Shrinkable<String> shrinkable = createShrinkableString("bbb", 0);
			TestingFalsifier<String> falsifier = aString -> aString.length() <= 1;
			String shrunkValue = shrinkToEnd(shrinkable, falsifier, null);
			assertThat(shrunkValue).isEqualTo("aa");
		}

		@Example
		void withFilterOnStringLength() {
			Shrinkable<String> shrinkable = createShrinkableString("cccc", 0);

			TestingFalsifier<String> falsifier = ignore -> false;
			Falsifier<String> filteredFalsifier = falsifier.withFilter(aString -> aString.length() % 2 == 0);

			ShrinkingSequence<String> sequence = shrinkable.shrink(filteredFalsifier);

			assertThat(sequence.next(count, reporter)).isTrue();
			assertThat(sequence.current().value()).isEqualTo("cccc");
			assertThat(sequence.next(count, reporter)).isTrue();
			assertThat(sequence.current().value()).isEqualTo("cc");
			assertThat(sequence.next(count, reporter)).isTrue();
			assertThat(sequence.current().value()).isEqualTo("cc");
			assertThat(sequence.next(count, reporter)).isTrue();
			assertThat(sequence.current().value()).isEqualTo("");
			assertThat(sequence.next(count, reporter)).isFalse();

			assertThat(counter.get()).isEqualTo(4);
		}

		@Example
		void withFilterOnStringContents() {
			Shrinkable<String> shrinkable = createShrinkableString("ddd", 0);

			TestingFalsifier<String> falsifier = String::isEmpty;
			Falsifier<String> filteredFalsifier = falsifier //
															.withFilter(aString -> aString.startsWith("d") || aString.startsWith("b"));

			String shrunkValue = shrinkToEnd(shrinkable, filteredFalsifier, null);
			assertThat(shrunkValue).isEqualTo("b");
		}

		@Example
		void longString() {
			List<Shrinkable<Character>> elementShrinkables =
				IntStream.range(0, 1000) //
						 .mapToObj(aChar -> new OneStepShrinkable(aChar, 0)) //
						 .map(shrinkableInt -> shrinkableInt.map(anInt -> (char) (int) anInt)) //
						 .collect(Collectors.toList());

			Shrinkable<String> shrinkable = new ShrinkableString(elementShrinkables, 5);
			String shrunkValue = shrinkToEnd(shrinkable, (TestingFalsifier<String>) String::isEmpty, null);
			assertThat(shrunkValue).hasSize(5);
		}

	}

	public static Shrinkable<String> createShrinkableString(String aString, int minSize) {
		List<Shrinkable<Character>> elementShrinkables = aString //
																 .chars() //
																 .mapToObj(aChar -> new OneStepShrinkable(aChar, 'a')) //
																 .map(shrinkable -> shrinkable.map(anInt -> (char) (int) anInt)) //
																 .collect(Collectors.toList());

		return new ShrinkableString(elementShrinkables, minSize);
	}

}
