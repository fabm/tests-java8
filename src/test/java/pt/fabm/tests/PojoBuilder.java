package pt.fabm.tests;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class PojoBuilder<T> {
    private Supplier<T> supplier;
    private List<Consumer<T>> listSetters = new ArrayList<>();

    public PojoBuilder(Supplier<T> supplier) {
        this.supplier = supplier;
    }

    public <V> PojoBuilder<T> with(final BiConsumer<T, V> setter, V value) throws InvalidParameter {
        Consumer<T> c = instance -> setter.accept(instance, value);
        listSetters.add(c);
        return this;
    }

    public <V> PojoBuilder<T> with(final BiConsumer<T, V> setter, Predicate<V> predicate, V value)
            throws InvalidParameter {
        return with(setter, predicate, null, value);
    }

    public <V> PojoBuilder<T> with(final BiConsumer<T, V> setter, Predicate<V> predicate, String name, V value)
            throws InvalidParameter {
        if (!predicate.test(value)) {
            throw new InvalidParameter(name);
        }
        Consumer<T> c = instance -> setter.accept(instance, value);
        listSetters.add(c);
        return this;
    }

    public T build() {
        T value = supplier.get();
        listSetters.forEach(e -> e.accept(value));
        return value;
    }

}
