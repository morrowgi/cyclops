package com.aol.cyclops.clojure.collections;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import com.aol.cyclops2.data.collections.extensions.lazy.immutable.LazyPSetX;
import cyclops.function.Reducer;
import cyclops.stream.ReactiveSeq;
import org.jooq.lambda.tuple.Tuple2;
import org.pcollections.PSet;



import clojure.lang.PersistentHashSet;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.Wither;


@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ClojureHashPSet<T> extends AbstractSet<T>implements PSet<T> {

    /**
     * Create a LazyPSetX from a Stream
     * 
     * @param stream to construct a LazyQueueX from
     * @return LazyPSetX
     */
    public static <T> LazyPSetX<T> fromStream(Stream<T> stream) {
        Reducer<PSet<T>> r = toPSet();
        return new LazyPSetX<T>(null, ReactiveSeq.fromStream(stream), r);
    }

    /**
     * Create a LazyPSetX that contains the Integers between start and end
     * 
     * @param start
     *            Number of range to start from
     * @param end
     *            Number for range to end at
     * @return Range SetX
     */
    public static LazyPSetX<Integer> range(int start, int end) {
        return fromStream(ReactiveSeq.range(start, end));
    }

    /**
     * Create a LazyPSetX that contains the Longs between start and end
     * 
     * @param start
     *            Number of range to start from
     * @param end
     *            Number for range to end at
     * @return Range SetX
     */
    public static LazyPSetX<Long> rangeLong(long start, long end) {
        return fromStream(ReactiveSeq.rangeLong(start, end));
    }

    /**
     * Unfold a function into a SetX
     * 
     * <pre>
     * {@code 
     *  LazyPSetX.unfold(1,i->i<=6 ? Optional.of(Tuple.tuple(i,i+1)) : Optional.empty());
     * 
     * //(1,2,3,4,5)
     * 
     * }</pre>
     * 
     * @param seed Initial value 
     * @param unfolder Iteratively applied function, terminated by an empty Optional
     * @return SetX generated by unfolder function
     */
    public static <U, T> LazyPSetX<T> unfold(U seed, Function<? super U, Optional<Tuple2<T, U>>> unfolder) {
        return fromStream(ReactiveSeq.unfold(seed, unfolder));
    }

    /**
     * Generate a LazyPSetX from the provided Supplier up to the provided limit number of times
     * 
     * @param limit Max number of elements to generate
     * @param s Supplier to generate SetX elements
     * @return SetX generated from the provided Supplier
     */
    public static <T> LazyPSetX<T> generate(long limit, Supplier<T> s) {

        return fromStream(ReactiveSeq.generate(s)
                                     .limit(limit));
    }

    /**
     * Create a LazyPSetX by iterative application of a function to an initial element up to the supplied limit number of times
     * 
     * @param limit Max number of elements to generate
     * @param seed Initial element
     * @param f Iteratively applied to each element to generate the next element
     * @return SetX generated by iterative application
     */
    public static <T> LazyPSetX<T> iterate(long limit, final T seed, final UnaryOperator<T> f) {
        return fromStream(ReactiveSeq.iterate(seed, f)
                                     .limit(limit));
    }

    /**
     * <pre>
     * {@code 
     * PSet<Integer> q = JSPSet.<Integer>toPSet()
                                     .mapReduce(Stream.of(1,2,3,4));
     * 
     * }
     * </pre>
     * @return Reducer for PSet
     */
    public static <T> Reducer<PSet<T>> toPSet() {
        return Reducer.<PSet<T>> of(ClojureHashPSet.emptyPSet(), (final PSet<T> a) -> b -> a.plusAll(b),
                                    (final T x) -> ClojureHashPSet.singleton(x));
    }

    public static <T> ClojureHashPSet<T> fromSet(PersistentHashSet set) {
        return new ClojureHashPSet<>(
                                     set);
    }

    public static <T> ClojureHashPSet<T> emptyPSet() {

        return new ClojureHashPSet<>(
                                     PersistentHashSet.EMPTY);
    }

    public static <T> LazyPSetX<T> empty() {

        return fromPSet(new ClojureHashPSet<T>(
                                                         PersistentHashSet.EMPTY),
                                  toPSet());
    }
    private static <T> LazyPSetX<T> fromPSet(PSet<T> ts, Reducer<PSet<T>> pSetReducer) {
        return new LazyPSetX<T>(ts,null,pSetReducer);
    }


    public static <T> LazyPSetX<T> singleton(T t) {
        return of(t);
    }

    public static <T> LazyPSetX<T> of(T... t) {

        return fromPSet(new ClojureHashPSet<>(
                                                        PersistentHashSet.create(t)),
                                  toPSet());
    }

    public static <T> LazyPSetX<T> PSet(PersistentHashSet q) {
        return fromPSet(new ClojureHashPSet<T>(
                                                         q),
                                  toPSet());
    }

    @SafeVarargs
    public static <T> LazyPSetX<T> PSet(T... elements) {
        return fromPSet(of(elements), toPSet());
    }

    @Wither
    private final PersistentHashSet set;

    @Override
    public ClojureHashPSet<T> plus(T e) {

        return withSet((PersistentHashSet) set.cons(e));
    }

    @Override
    public ClojureHashPSet<T> plusAll(Collection<? extends T> l) {

        PersistentHashSet use = set;
        for (T next : l)
            use = (PersistentHashSet) use.cons(next);
        return withSet(use);

    }

    @Override
    public PSet<T> minus(Object e) {
        return withSet((PersistentHashSet) set.disjoin(e));

    }

    @Override
    public PSet<T> minusAll(Collection<?> s) {
        PersistentHashSet use = set;
        for (Object next : s)
            use = (PersistentHashSet) use.disjoin(next);
        return withSet(use);
    }

    @Override
    public int size() {
        return set.count();
    }

    @Override
    public Iterator<T> iterator() {
        return set.iterator();
    }

}
