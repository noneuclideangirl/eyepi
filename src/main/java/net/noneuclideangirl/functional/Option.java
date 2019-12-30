package net.noneuclideangirl.functional;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class Option<T> {
    private static None<?> NONE;

    public static <T> Option<T> of(T value) {
        return value == null ? none() : some(value);
    }

    public static <T> Option<T> some(T value) {
        return new Some<>(value);
    }

    public static <T> Option<T> none() {
        if (NONE == null) {
            NONE = new None<>();
        }
        @SuppressWarnings("unchecked") None<T> none = (None<T>) NONE;
        return none;
    }

    public static <T> Option<T> ifThen(boolean condition, T value) {
        if (condition) {
            return Option.of(value);
        } else {
            return Option.none();
        }
    }

    public abstract T unwrap();
    public abstract T unwrapOr(T defaultVal);

    public abstract <R> Option<R> map(Function<T, R> func);
    public abstract Option<T> or(Option<T> ifNone);
    public T orElse(T value) {
        return or(Option.some(value)).unwrap();
    }
    public <R> R matchThen(R ifSome, R ifNone) {
        return matchThen(Functional.constVal(ifSome), () -> ifNone);
    }
    public abstract <R> R matchThen(Function<T, R> ifSome, Supplier<R> ifNone);
    public Option<T> match(Consumer<T> ifSome, Runnable ifNone) {
        matchThen(val -> {
            ifSome.accept(val);
            return null;
        }, () -> {
            ifNone.run();
            return null;
        });
        return this;
    }
    public abstract <R> Option<R> andThen(Function<T, Option<R>> func);
    public Option<T> ifNone(Runnable action) {
        match(Functional.consume(), action);
        return this;
    }
    public Option<T> ifSome(Consumer<T> consumer) {
        match(consumer, Functional.noop);
        return this;
    }

    public boolean isSome() {
        return matchThen(true, false);
    }

    private static class None<T> extends Option<T> {
        @Override
        public T unwrap() {
            throw new RuntimeException("Unwrapped empty Option");
        }

        @Override
        public T unwrapOr(T defaultVal) {
            return defaultVal;
        }

        @Override
        public <R> Option<R> map(Function<T, R> func) {
            return Option.none();
        }

        @Override
        public Option<T> or(Option<T> ifNone) {
            return ifNone;
        }

        @Override
        public <R> R matchThen(Function<T, R> ifSome, Supplier<R> ifNone) {
            return ifNone.get();
        }

        @Override
        public <R> Option<R> andThen(Function<T, Option<R>> func) {
            return Option.none();
        }

        @Override
        public String toString() {
            return "Option.None";
        }
    }
    private static class Some<T> extends Option<T> {
        private final T value;

        private Some(T value) {
            this.value = value;
        }

        @Override
        public T unwrap() {
            return value;
        }

        @Override
        public T unwrapOr(T defaultVal) {
            return value;
        }

        @Override
        public <R> Option<R> map(Function<T, R> func) {
            return Option.some(func.apply(value));
        }

        @Override
        public Option<T> or(Option<T> ifNone) {
            return this;
        }

        @Override
        public <R> R matchThen(Function<T, R> ifSome, Supplier<R> ifNone) {
            return ifSome.apply(value);
        }

        @Override
        public <R> Option<R> andThen(Function<T, Option<R>> func) {
            return func.apply(value);
        }

        @Override
        public String toString() {
            return "Option.Some(" + value + ")";
        }
    }
}
