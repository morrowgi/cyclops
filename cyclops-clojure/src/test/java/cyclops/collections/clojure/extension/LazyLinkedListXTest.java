package cyclops.collections.clojure.extension;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import com.aol.cyclops2.data.collections.extensions.FluentCollectionX;

import cyclops.collections.immutable.BagX;
import cyclops.collections.immutable.LinkedListX;
import cyclops.collections.mutable.ListX;
import cyclops.companion.Semigroups;
import org.jooq.lambda.tuple.Tuple2;
import org.junit.Test;

import cyclops.collections.clojure.ClojureListX;


import reactor.core.publisher.Flux;

public class LazyLinkedListXTest extends AbstractOrderDependentCollectionXTest  {

    @Override
    public <T> FluentCollectionX<T> of(T... values) {
        LinkedListX<T> list = ClojureListX.empty();
        for (T next : values) {
            list = list.plus(list.size(), next);
        }
        System.out.println("List " + list);
        return list;

    }
    @Test
    public void combineNoOrderOd(){
        assertThat(of(1,2,3)
                   .combine((a, b)->a.equals(b), Semigroups.intSum)
                   .toListX(),equalTo(ListX.of(1,2,3)));
                   
    }

    @Test
    public void onEmptySwitch() {
        assertThat(ClojureListX.empty()
                          .onEmptySwitch(() -> LinkedListX.of(1, 2, 3)),
                   equalTo(LinkedListX.of(1, 2, 3)));
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.aol.cyclops.functions.collections.extensions.AbstractCollectionXTest#
     * empty()
     */
    @Override
    public <T> FluentCollectionX<T> empty() {
        return ClojureListX.empty();
    }

    

    @Test
    public void remove() {

        ClojureListX.of(1, 2, 3)
               .minusAll(BagX.of(2, 3))
               .flatMapP(i -> Flux.just(10 + i, 20 + i, 30 + i));

    }

    @Override
    public FluentCollectionX<Integer> range(int start, int end) {
        return ClojureListX.range(start, end);
    }

    @Override
    public FluentCollectionX<Long> rangeLong(long start, long end) {
        return ClojureListX.rangeLong(start, end);
    }

    @Override
    public <T> FluentCollectionX<T> iterate(int times, T seed, UnaryOperator<T> fn) {
        return ClojureListX.iterate(times, seed, fn);
    }

    @Override
    public <T> FluentCollectionX<T> generate(int times, Supplier<T> fn) {
        return ClojureListX.generate(times, fn);
    }

    @Override
    public <U, T> FluentCollectionX<T> unfold(U seed, Function<? super U, Optional<Tuple2<T, U>>> unfolder) {
        return ClojureListX.unfold(seed, unfolder);
    }
}
