package net.noneuclideangirl.functional;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class Functional {
    private Functional() {}

    public static <T extends R, R> Function <T, R> id() { return x -> x; }
    public static <T> Supplier<T> supply(T val) { return () -> val; }
    public static <T> Consumer<T> consume() { return __ -> {}; }
    public static final Runnable noop = () -> {};
    public static <T, R> Function<T, R> constVal(R val) { return __ -> val; }
}
