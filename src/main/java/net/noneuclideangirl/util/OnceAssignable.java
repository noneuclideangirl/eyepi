package net.noneuclideangirl.util;

public class OnceAssignable<T> {
    private T value;
    private boolean hasValue = false;

    public void set(T value) {
        if (hasValue) {
            throw new RuntimeException("Attempted to assign to OnceAssignable with existing value: " + value);
        } else {
            hasValue = true;
            this.value = value;
        }
    }

    public T get() {
        if (!hasValue) {
            throw new RuntimeException("Attempted to get value of OnceAssignable with no value");
        } else {
            return value;
        }
    }
}
