package pt.fabm.tests;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
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

        setterCheckers.add(new SetterChecker<Integer>()
                .withParameterName("num")
                .withValue(2)
                .withPredicate(e -> e > 3)
                .withSetter(pojo::setNum)
        );

        setterCheckers.add(new SetterChecker<String>()
                .withParameterName("text")
                .withPredicate(notEmpty)
                .withValue("hello world")
                .withSetter(pojo::setText)
        );

        Optional<SetterChecker<?>> failChecker = setterCheckers.stream().filter(e -> !e.checkedSet()).findAny();


        Assert.assertTrue(failChecker.isPresent());
        Assert.assertEquals("num",failChecker.get().getParameterName());


    }

}
