package net.noneuclideangirl.functional;

import java.util.function.Consumer;
import java.util.function.Function;

public class Functional {
    private Functional() {}

    public static <T extends R, R> Function <T, R> id() { return x -> x; }
    public static <T> Consumer<T> consume() { return __ -> {}; }
    public static final Runnable noop = () -> {};
    public static <T, R> Function<T, R> constVal(R val) { return __ -> val; }
}
