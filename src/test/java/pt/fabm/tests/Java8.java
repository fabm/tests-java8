package pt.fabm.tests;

import org.junit.Assert;
import org.junit.Test;

import java.io.Closeable;
import java.io.IOException;
import java.util.*;
import java.util.function.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Java8 {
    private Predicate<String> notEmpty;

    public <V> void validatedSet(V value, Consumer<V> setter, Predicate<V> predicate) throws InvalidParameter {
        validatedSet(value, setter, predicate, null);
    }

    public <V> void validatedSet(V value, Consumer<V> setter, Predicate<V> predicate, String name) throws InvalidParameter {
        if (predicate.test(value)) {
            setter.accept(value);
        } else {
            final InvalidParameter invalidParameter = new InvalidParameter();
            invalidParameter.setName(name);
            throw invalidParameter;
        }
    }

    @Test
    public void testPojo() {
        Pojo pojo = new GenericBuilder<>(Pojo::new)
                .with(Pojo::setNum, Objects::nonNull, 1)
                .with(Pojo::setText, notEmpty, "example")
                .build();

        Assert.assertEquals(1, pojo.getNum());
        Assert.assertEquals("example", pojo.getText());

        pojo = null;
        try {
            pojo = new GenericBuilder<>(Pojo::new)
                    .with(Pojo::setNum, Objects::nonNull, 1)
                    .with(Pojo::setText, notEmpty, "text", "")
                    .build();
            Assert.fail();
        } catch (InvalidParameter invalidParameter) {
            Assert.assertEquals("text", invalidParameter.getName());
        }

        Assert.assertNull(pojo);

    }

    @Test
    public void testsPredicates() {
        Pojo pojo = new Pojo();

        notEmpty = e -> e != null && !e.isEmpty();

        validatedSet(2, pojo::setNum, e -> e != null && e >= 2);
        validatedSet("Hello world", pojo::setText, notEmpty.and(e -> e.length() > 2));
        validatedSet(3, pojo::setNum, e -> e > 2);

        try {
            validatedSet(5, pojo::setNum, e -> e > 6, "num");
            Assert.fail();
        } catch (InvalidParameter e) {
            Assert.assertEquals(e.getName(), "num");
        }

    }

    @Test
    public void testsSetterChecker() {
        Pojo pojo = new Pojo();


        List<SetterChecker<?>> setterCheckers = new ArrayList<>();

        Consumer<Object> bs = e -> System.out.println("-->" + e);

        setterCheckers.add(new SetterChecker<Integer>()
                .withParameterName("num")
                .withValue(2)
                .withPredicate(e -> e > 3)
                .withSetter(pojo::setNum)
                .withBeforeSet(bs)
        );

        setterCheckers.add(new SetterChecker<String>()
                .withParameterName("text")
                .withPredicate(notEmpty)
                .withValue("hello world")
                .withSetter(pojo::setText)
                .withBeforeSet(bs)
        );

        Optional<SetterChecker<?>> failChecker = setterCheckers.stream().filter(e -> !e.checkedSet()).findAny();


        Assert.assertTrue(failChecker.isPresent());
        Assert.assertEquals("num", failChecker.get().getParameterName());


    }

    @Test
    public void testConsumersList() {
        List<? super Object> values = new ArrayList<>();

        Function<? super Object, ? super Object> format = e -> {
            if (e == null) {
                return "is null";
            }
            if (e instanceof Optional) {
                final Optional optional = (Optional) e;
                Assert.assertTrue(optional.isPresent());
                Object value = optional.get();
                return "this is an optional " + value.toString() + " with the value " + value;
            }
            return "class:" + e.getClass().getCanonicalName() + " and value " + e;
        };

        List<String> logs = new ArrayList<>(4);

        IntConsumer lastPrint = i -> logs.add("index:" + i + " object:" + format.apply(values.get(i)));

        values.add(1);
        values.add("2");
        values.add(Optional.of(3));
        values.add(null);

        class MethodClass {
            private Integer val0;
            private String val1;
            @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
            private Optional<Integer> val2;
            private Object val3;

            private Integer getVal0() {
                return val0;
            }

            private void setVal0(Integer val0) {
                this.val0 = val0;
            }

            private String getVal1() {
                return val1;
            }

            private void setVal1(String val1) {
                this.val1 = val1;
            }

            private Optional<Integer> getVal2() {
                return val2;
            }

            private void setVal2(@SuppressWarnings("OptionalUsedAsFieldOrParameterType") Optional<Integer> val2) {
                this.val2 = val2;
            }

            private Object getVal3() {
                return val3;
            }

            private void setVal3(Object val3) {
                this.val3 = val3;
            }
        }

        MethodClass aClass = new MethodClass();

        class InnerBuilder {
            private Stream.Builder<String> strValues = Stream.builder();

            private <T> void consume(Consumer<T> consumer, T value) {
                strValues.add(Objects.toString(value));
                consumer.accept(value);
            }

            @Override
            public String toString() {
                return strValues.build().collect(Collectors.joining("\",\"", "[\"", "\"]"));
            }
        }
        InnerBuilder innerBuilder = new InnerBuilder();

        innerBuilder.consume(aClass::setVal0, 1);
        innerBuilder.consume(aClass::setVal1, "2");
        innerBuilder.consume(aClass::setVal2, Optional.of(3));
        innerBuilder.consume(aClass::setVal3, null);

        Assert.assertEquals("[\"1\",\"2\",\"Optional[3]\",\"null\"]", innerBuilder.toString());

        List<IntConsumer> setters = new ArrayList<>();
        setters.add(i -> aClass.setVal0((Integer) values.get(i)));
        setters.add(i -> aClass.setVal1((String) values.get(i)));
        setters.add(i -> aClass.setVal2((Optional<Integer>) values.get(i)));
        setters.add(i -> aClass.setVal3(values.get(i)));


        final IntConsumer intConsumer = i -> setters.get(i)
                .andThen(lastPrint)
                .accept(i);

        IntStream.range(0, 4).forEach(intConsumer);

        Assert.assertEquals(Integer.valueOf(1), aClass.getVal0());
        Assert.assertEquals("2", aClass.getVal1());
        Assert.assertEquals(Optional.of(3), aClass.getVal2());
        Assert.assertNull(aClass.getVal3());

        Stream<String> stream = IntStream.range(0, 4)
                .mapToObj(values::get)
                .map(Objects::toString);

        final String collect = stream.collect(Collectors.joining("\",\"", "[\"", "\"]"));
        Assert.assertEquals("[\"1\",\"2\",\"Optional[3]\",\"null\"]", collect);


        Assert.assertEquals("index:0 object:class:java.lang.Integer and value 1", logs.get(0));
        Assert.assertEquals("index:1 object:class:java.lang.String and value 2", logs.get(1));
        Assert.assertEquals("index:2 object:this is an optional 3 with the value 3", logs.get(2));
        Assert.assertEquals("index:3 object:is null", logs.get(3));
    }


    @Test
    public void testCloseResources() {
        Map<String, Integer> mapAsserts = new HashMap<>();
        mapAsserts.put("current", 0);

        class Inner implements Closeable {

            @Override
            public void close() throws IOException {
                Integer current = mapAsserts.get("current");
                mapAsserts.put("inner", current);
                mapAsserts.put("current", ++current);
            }
        }

        class Outer implements Closeable {

            private Outer() {
            }

            private Inner getInner() {
                return new Inner();
            }

            @Override
            public void close() throws IOException {
                Integer current = mapAsserts.get("current");
                mapAsserts.put("outer", current);
                mapAsserts.put("current", ++current);
            }
        }

        try (
                Outer outer = new Outer();
                Inner ignored = outer.getInner();
        ) {
            Assert.assertTrue(Closeable.class.isAssignableFrom(Outer.class));
            Assert.assertTrue(Closeable.class.isAssignableFrom(Inner.class));
        } catch (IOException e) {
            e.printStackTrace();
        }
        Assert.assertEquals(Integer.valueOf(0), mapAsserts.get("inner"));
        Assert.assertEquals(Integer.valueOf(1), mapAsserts.get("outer"));
    }


}
