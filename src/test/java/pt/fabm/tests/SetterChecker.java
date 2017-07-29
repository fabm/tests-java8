package pt.fabm.tests;

import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class SetterChecker<V> {
    private Supplier<V> supplier;
    private Predicate<V> predicate;
    private Consumer<V> setter;
    private String parameterName;

    public SetterChecker() {
    }


    public Supplier<V> getSupplier() {
        return supplier;
    }

    public SetterChecker<V> withSupplier(Supplier<V> supplier) {
        this.supplier = supplier;
        return this;
    }

    public SetterChecker<V> withParameterName(String parameterName) {
        this.parameterName = parameterName;
        return this;
    }

    public Predicate<V> getPredicate() {
        return predicate;
    }

    public SetterChecker<V> withPredicate(Predicate<V> predicate) {
        this.predicate = predicate;
        return this;
    }

    public Consumer<V> getSetter() {
        return setter;
    }

    public SetterChecker<V> withSetter(Consumer<V> setter) {
        this.setter = setter;
        return this;
    }

    public void checkedSet() throws InvalidParameter{
        V value = supplier.get();
        if (!predicate.test(value)){
            throw new InvalidParameter(parameterName);
        }else{
            getSetter().accept(value);
        }
    }

}
