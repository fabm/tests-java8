package pt.fabm.tests;

import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class SetterChecker<V> {
    private V value;
    private Predicate<V> predicate;
    private Consumer<V> setter;
    private String parameterName;

    public SetterChecker() {
    }


    public String getParameterName() {
        return parameterName;
    }

    public V getValue() {
        return value;
    }

    public SetterChecker<V> withValue(V value) {
        this.value = value;
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

    public boolean checkedSet() throws InvalidParameter {
        if (!predicate.test(value)) {
            return false;
        }
        getSetter().accept(value);
        return true;
    }


}
