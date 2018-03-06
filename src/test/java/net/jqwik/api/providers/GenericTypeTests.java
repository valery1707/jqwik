package net.jqwik.api.providers;

import net.jqwik.*;
import net.jqwik.api.*;
import net.jqwik.api.Tuples.*;
import net.jqwik.api.constraints.*;
import net.jqwik.support.*;

import java.lang.reflect.*;
import java.util.*;

import static net.jqwik.api.providers.GenericType.*;
import static org.assertj.core.api.Assertions.*;

class GenericTypeTests {

	@Example
	void simpleType() {
		GenericType stringType = GenericType.of(String.class);
		assertThat(stringType.getRawType()).isEqualTo(String.class);
		assertThat(stringType.isOfType(String.class)).isTrue();
		assertThat(stringType.isGeneric()).isFalse();
		assertThat(stringType.isArray()).isFalse();
		assertThat(stringType.isEnum()).isFalse();
	}

	@Example
	void parameterizedType() {
		GenericType tupleType = GenericType.of(Tuple2.class, of(String.class), of(Integer.class));
		assertThat(tupleType.getRawType()).isEqualTo(Tuple2.class);
		assertThat(tupleType.isOfType(Tuple2.class)).isTrue();
		assertThat(tupleType.isGeneric()).isTrue();
		assertThat(tupleType.isArray()).isFalse();
		assertThat(tupleType.isEnum()).isFalse();
	}

	@Example
	void specifyingTypeParametersForNonGenericTypeFails() {
		assertThatThrownBy(() -> GenericType.of(String.class, of(String.class))).isInstanceOf(JqwikException.class);
	}

	@Example
	void forType() throws NoSuchMethodException {
		class LocalClass {
			@SuppressWarnings("WeakerAccess")
			public Tuple2<String, Integer> withReturn() { return null; }
		}

		Type type = LocalClass.class.getMethod("withReturn").getAnnotatedReturnType().getType();
		GenericType tupleType = GenericType.forType(type);
		assertThat(tupleType.getRawType()).isEqualTo(Tuple2.class);
		assertThat(tupleType.isOfType(Tuple2.class)).isTrue();
		assertThat(tupleType.isGeneric()).isTrue();
		assertThat(tupleType.isArray()).isFalse();
		assertThat(tupleType.isEnum()).isFalse();
	}

	@Group
	class ForParameter {
		@Example
		void genericParameter() throws NoSuchMethodException {
			class LocalClass {
				@SuppressWarnings("WeakerAccess")
				public void withParameter(Tuple2<String, Integer> tuple) {}
			}

			Method method = LocalClass.class.getMethod("withParameter", Tuple2.class);
			MethodParameter parameter = JqwikReflectionSupport.getMethodParameters(method)[0];
			GenericType tupleType = GenericType.forParameter(parameter);
			assertThat(tupleType.getRawType()).isEqualTo(Tuple2.class);
			assertThat(tupleType.isOfType(Tuple2.class)).isTrue();
			assertThat(tupleType.isGeneric()).isTrue();
			assertThat(tupleType.isArray()).isFalse();
			assertThat(tupleType.isEnum()).isFalse();
		}

		@Example
		void annotations() throws NoSuchMethodException {
			class LocalClass {
				@SuppressWarnings("WeakerAccess")
				public void withList(@Size(max = 2) List list) {}
			}

			Method method = LocalClass.class.getMethod("withList", List.class);
			MethodParameter parameter = JqwikReflectionSupport.getMethodParameters(method)[0];
			GenericType parameterType = GenericType.forParameter(parameter);
			assertThat(parameterType.getRawType()).isEqualTo(List.class);
			assertThat(parameterType.getAnnotations().get(0)).isInstanceOf(Size.class);
		}

		@Example
		void annotatedTypeParameters() throws NoSuchMethodException {
			class LocalClass {
				@SuppressWarnings("WeakerAccess")
				public void withList(
					@Size(max = 5) List<
						@StringLength(max = 2)
						@CharRange(from = 'a', to = 'z') String
						> list
				) {}
			}

			Method method = LocalClass.class.getMethod("withList", List.class);
			MethodParameter parameter = JqwikReflectionSupport.getMethodParameters(method)[0];
			GenericType listType = GenericType.forParameter(parameter);
			assertThat(listType.getAnnotations().get(0)).isInstanceOf(Size.class);
			GenericType stringType = listType.getTypeArguments().get(0);
			assertThat(stringType.isOfType(String.class)).isTrue();
			assertThat(stringType.getAnnotations().get(0)).isInstanceOf(StringLength.class);
			assertThat(stringType.getAnnotations().get(1)).isInstanceOf(CharRange.class);
		}

	}

	@Group
	class Compatibility {

		@Example
		void primitiveTypes() {
			assertThat(of(int.class).isCompatibleWith(Integer.class)).isTrue();
			assertThat(of(int.class).isCompatibleWith(int.class)).isTrue();
			assertThat(of(Integer.class).isCompatibleWith(Integer.class)).isTrue();

			assertThat(of(Integer.class).isCompatibleWith(int.class)).isFalse();
			assertThat(of(int.class).isCompatibleWith(float.class)).isFalse();
		}

		@Example
		void rawTypes() {
			assertThat(of(Object.class).isCompatibleWith(Object.class)).isTrue();
			assertThat(of(Object.class).isCompatibleWith(String.class)).isFalse();
		}

		@Example
		void genericTypesToRawTypes() {
			GenericType target = of(List.class, of(String.class));

			assertThat(target.isCompatibleWith(List.class)).isTrue();
			assertThat(target.isCompatibleWith(Collection.class)).isFalse();
			assertThat(target.isCompatibleWith(Set.class)).isFalse();
		}

		@Example
		void plainTypeToWildcardType() throws NoSuchMethodException {
			class LocalClass {
				@SuppressWarnings("WeakerAccess")
				public void withParameter(List<?> list) {}
			}

			Method method = LocalClass.class.getMethod("withParameter", List.class);
			MethodParameter parameter = JqwikReflectionSupport.getMethodParameters(method)[0];

			GenericType target = forParameter(parameter);
			GenericType provided = of(List.class);

			assertThat(target.isCompatibleWith(provided)).isTrue();
		}

		@Example
		void genericTypesAmongEachOther() {
			GenericType target = of(List.class, of(String.class));
			GenericType provided = of(List.class, of(String.class));

			assertThat(target.isCompatibleWith(provided)).isTrue();

			assertThat(target.isCompatibleWith(of(List.class, of(Object.class)))).isFalse();
			assertThat(of(List.class, of(Object.class)).isCompatibleWith(provided)).isFalse();
		}
	}

}
