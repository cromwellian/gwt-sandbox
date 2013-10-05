package java.util.function;

@FunctionalInterface
public interface Function<T, R> {
    default <V> Function<T,V> andThen(Function<? super R,? extends V> after) {
      return (T t) -> after.apply(apply(t));
    }
    R	apply(T t);
    default <V> Function<V,R> compose(Function<? super V,? extends T> before) {
       return (V v) -> apply(before.apply(v));
    }
    static <T> Function<T,T> identity() {
        return (T x) -> x;
    }
}