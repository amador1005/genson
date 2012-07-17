package org.genson.reflect;

import java.io.IOException;
import java.lang.reflect.GenericArrayType;
import java.util.Date;
import java.util.List;

import org.genson.Factory;
import org.genson.Genson;
import org.genson.TransformationException;
import org.genson.annotation.JsonIgnore;
import org.genson.annotation.JsonProperty;
import org.genson.convert.BasicConvertersFactory;
import org.genson.convert.Converter;
import org.genson.convert.DefaultConverters;
import org.genson.convert.DefaultConverters.CollectionConverter;
import org.junit.Test;

import static org.junit.Assert.*;

public class BeanDescriptorTest {

	private static class ThatObject {
		@SuppressWarnings("unused")
		String aString;
		@SuppressWarnings("unused")
		int aPrimitive;
		@SuppressWarnings("unused")
		List<Date> listOfDates;

		@SuppressWarnings("unused")
		public ThatObject(AnotherObject anotherObject) {
		}
	}

	private static class AnotherObject {
		@SuppressWarnings("unused")
		public AnotherObject() {
		}
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testConverterChain() {
		Genson genson = new Genson.Builder() {
			@Override
			protected Factory<Converter<?>> createConverterFactory() {
				return new BasicConvertersFactory(getSerializersMap(), getDeserializersMap(), getFactories(),
						getBeanDescriptorProvider());
			}
		}.setWithDebugInfoPropertyNameResolver(true).create();

		@SuppressWarnings("rawtypes")
		BeanDescriptor<ThatObject> pDesc = (BeanDescriptor) genson
				.provideConverter(ThatObject.class);
		assertEquals(DefaultConverters.StringConverter.class,
				pDesc.mutableProperties.get("aString").propertyDeserializer.getClass());
		assertEquals(DefaultConverters.PrimitiveConverterFactory.intConverter.class,
				pDesc.mutableProperties.get("aPrimitive").propertyDeserializer.getClass());
		assertEquals(DefaultConverters.CollectionConverter.class,
				pDesc.mutableProperties.get("listOfDates").propertyDeserializer.getClass());
		CollectionConverter<Date> listOfDateConverter = (CollectionConverter<Date>) pDesc.mutableProperties
				.get("listOfDates").propertyDeserializer;
		assertEquals(DefaultConverters.DateConverter.class, listOfDateConverter
				.getElementConverter().getClass());

		assertEquals(BeanDescriptor.class,
				pDesc.mutableProperties.get("anotherObject").propertyDeserializer.getClass());
	}

	@Test
	public void genericTypeTest() throws TransformationException, IOException {
		BaseBeanDescriptorProvider provider = new BaseBeanDescriptorProvider(
				new BeanMutatorAccessorResolver.StandardMutaAccessorResolver(),
				new PropertyNameResolver.ConventionalBeanPropertyNameResolver(), true, true);

		BeanDescriptor<SpecilizedClass> bd = provider.provideBeanDescriptor(SpecilizedClass.class,
				new Genson());
		assertEquals(B.class, getAccessor("t", bd).type);
		assertEquals(B.class,
				((GenericArrayType) getAccessor("tArray", bd).type).getGenericComponentType());
		assertEquals(Double.class, getAccessor("value", bd).type);
	}

	@Test
	public void jsonWithJsonIgnore() throws SecurityException, NoSuchFieldException {
		BeanMutatorAccessorResolver strategy = new BeanMutatorAccessorResolver.StandardMutaAccessorResolver();
		assertFalse(strategy.isAccessor(
				ClassWithIgnoredProperties.class.getDeclaredField("ignore"),
				ClassWithIgnoredProperties.class));
		assertFalse(strategy.isMutator(ClassWithIgnoredProperties.class.getDeclaredField("ignore"),
				ClassWithIgnoredProperties.class));
		assertTrue(strategy.isAccessor(ClassWithIgnoredProperties.class.getDeclaredField("a"),
				ClassWithIgnoredProperties.class));
		assertFalse(strategy.isMutator(ClassWithIgnoredProperties.class.getDeclaredField("a"),
				ClassWithIgnoredProperties.class));
		assertTrue(strategy.isMutator(ClassWithIgnoredProperties.class.getDeclaredField("b"),
				ClassWithIgnoredProperties.class));
		assertFalse(strategy.isAccessor(ClassWithIgnoredProperties.class.getDeclaredField("b"),
				ClassWithIgnoredProperties.class));
	}

	@Test
	public void jsonInclusionWithJsonProperty() throws SecurityException, NoSuchFieldException {
		BeanMutatorAccessorResolver strategy = new BeanMutatorAccessorResolver.StandardMutaAccessorResolver();
		assertTrue(strategy.isAccessor(ClassWithIgnoredProperties.class.getDeclaredField("p"),
				ClassWithIgnoredProperties.class));
		assertTrue(strategy.isMutator(ClassWithIgnoredProperties.class.getDeclaredField("p"),
				ClassWithIgnoredProperties.class));
		assertFalse(strategy.isAccessor(ClassWithIgnoredProperties.class.getDeclaredField("q"),
				ClassWithIgnoredProperties.class));
		assertTrue(strategy.isMutator(ClassWithIgnoredProperties.class.getDeclaredField("q"),
				ClassWithIgnoredProperties.class));
		assertFalse(strategy.isMutator(ClassWithIgnoredProperties.class.getDeclaredField("r"),
				ClassWithIgnoredProperties.class));
		assertTrue(strategy.isAccessor(ClassWithIgnoredProperties.class.getDeclaredField("r"),
				ClassWithIgnoredProperties.class));
	}

	PropertyAccessor<?, ?> getAccessor(String name, BeanDescriptor<?> bd) {
		for (PropertyAccessor<?, ?> a : bd.accessibleProperties)
			if (name.equals(a.name))
				return a;
		return null;
	}

	public static class B {
		public String v;
	}

	public static class ClassWithGenerics<T, E extends Number> {
		public T t;
		public T[] tArray;
		public E value;

		public void setT(T t) {
			this.t = t;
		}
	}

	public static class SpecilizedClass extends ClassWithGenerics<B, Double> {

	}

	public class ClassWithIgnoredProperties {
		@JsonIgnore
		public String ignore;
		@JsonIgnore(serialize = true)
		String a;
		@JsonIgnore(deserialize = true)
		public String b;

		@JsonProperty
		transient int p;
		@SuppressWarnings("unused")
		@JsonProperty(serialize = false)
		private transient int q;
		@JsonProperty(deserialize = false)
		public transient int r;
	}
}
