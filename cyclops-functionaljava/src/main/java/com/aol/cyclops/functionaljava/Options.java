package com.aol.cyclops.functionaljava;


import com.aol.cyclops.functionaljava.FJWitness.option;
import com.aol.cyclops.functionaljava.hkt.OptionKind;
import com.aol.cyclops.functionaljava.hkt.typeclassess.instances.Instances;
import com.aol.cyclops2.data.collections.extensions.CollectionX;
import com.aol.cyclops2.hkt.Higher;
import com.aol.cyclops2.types.Value;
import com.aol.cyclops2.types.anyM.AnyMValue;
import cyclops.Monoids;
import cyclops.collections.ListX;
import cyclops.control.Maybe;
import cyclops.function.Fn3;
import cyclops.function.Fn4;
import cyclops.function.Monoid;
import cyclops.function.Reducer;
import cyclops.monads.AnyM;
import cyclops.monads.WitnessType;
import cyclops.monads.transformers.OptionalT;
import cyclops.stream.ReactiveSeq;
import cyclops.typeclasses.Pure;
import cyclops.typeclasses.comonad.Comonad;
import cyclops.typeclasses.foldable.Foldable;
import cyclops.typeclasses.functor.Functor;
import cyclops.typeclasses.instances.General;
import cyclops.typeclasses.monad.*;
import fj.data.Option;
import lombok.experimental.UtilityClass;
import org.reactivestreams.Publisher;

import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Utility class for working with JDK Optionals
 *
 * @author johnmcclean
 *
 */
@UtilityClass
public class Options {
    public static <T,W extends WitnessType<W>> OptionalT<W, T> liftM(Option<T> opt, W witness) {
        return OptionalT.of(witness.adapter().unit(opt.isSome()? Optional.of(opt.some()) : Optional.empty()));
    }
    public static <T> AnyMValue<option,T> anyM(Option<T> option) {
        return AnyM.ofValue(option, FJWitness.option.INSTANCE);
    }
    /**
     * Perform a For Comprehension over a Option, accepting 3 generating function.
     * This results in a four level nested internal iteration over the provided Options.
     *
     *  <pre>
     * {@code
     *
     *   import static com.aol.cyclops2.reactor.Options.forEach4;
     *
    forEach4(Option.just(1),
    a-> Option.just(a+1),
    (a,b) -> Option.<Integer>just(a+b),
    a                  (a,b,c) -> Option.<Integer>just(a+b+c),
    Tuple::tuple)
     *
     * }
     * </pre>
     *
     * @param value1 top level Option
     * @param value2 Nested Option
     * @param value3 Nested Option
     * @param value4 Nested Option
     * @param yieldingFunction Generates a result per combination
     * @return Option with a combined value generated by the yielding function
     */
    public static <T1, T2, T3, R1, R2, R3, R> Option<R> forEach4(Option<? extends T1> value1,
                                                                 Function<? super T1, ? extends Option<R1>> value2,
                                                                 BiFunction<? super T1, ? super R1, ? extends Option<R2>> value3,
                                                                 Fn3<? super T1, ? super R1, ? super R2, ? extends Option<R3>> value4,
                                                                 Fn4<? super T1, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {

        return value1.bind(in -> {

            Option<R1> a = value2.apply(in);
            return a.bind(ina -> {
                Option<R2> b = value3.apply(in,ina);
                return b.bind(inb -> {
                    Option<R3> c = value4.apply(in,ina,inb);
                    return c.map(in2 -> yieldingFunction.apply(in, ina, inb, in2));
                });

            });

        });

    }

    /**
     *
     * Perform a For Comprehension over a Option, accepting 3 generating function.
     * This results in a four level nested internal iteration over the provided Options.
     *
     * <pre>
     * {@code
     *
     *  import static com.aol.cyclops2.reactor.Options.forEach4;
     *
     *  forEach4(Option.just(1),
    a-> Option.just(a+1),
    (a,b) -> Option.<Integer>just(a+b),
    (a,b,c) -> Option.<Integer>just(a+b+c),
    (a,b,c,d) -> a+b+c+d <100,
    Tuple::tuple);
     *
     * }
     * </pre>
     *
     * @param value1 top level Option
     * @param value2 Nested Option
     * @param value3 Nested Option
     * @param value4 Nested Option
     * @param filterFunction A filtering function, keeps values where the predicate holds
     * @param yieldingFunction Generates a result per combination
     * @return Option with a combined value generated by the yielding function
     */
    public static <T1, T2, T3, R1, R2, R3, R> Option<R> forEach4(Option<? extends T1> value1,
                                                                 Function<? super T1, ? extends Option<R1>> value2,
                                                                 BiFunction<? super T1, ? super R1, ? extends Option<R2>> value3,
                                                                 Fn3<? super T1, ? super R1, ? super R2, ? extends Option<R3>> value4,
                                                                 Fn4<? super T1, ? super R1, ? super R2, ? super R3, Boolean> filterFunction,
                                                                 Fn4<? super T1, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {

        return value1.bind(in -> {

            Option<R1> a = value2.apply(in);
            return a.bind(ina -> {
                Option<R2> b = value3.apply(in,ina);
                return b.bind(inb -> {
                    Option<R3> c = value4.apply(in,ina,inb);
                    return c.filter(in2->filterFunction.apply(in,ina,inb,in2))
                            .map(in2 -> yieldingFunction.apply(in, ina, inb, in2));
                });

            });

        });

    }

    /**
     * Perform a For Comprehension over a Option, accepting 2 generating function.
     * This results in a three level nested internal iteration over the provided Options.
     *
     *  <pre>
     * {@code
     *
     *   import static com.aol.cyclops2.reactor.Options.forEach3;
     *
    forEach3(Option.just(1),
    a-> Option.just(a+1),
    (a,b) -> Option.<Integer>just(a+b),
    Tuple::tuple)
     *
     * }
     * </pre>
     *
     * @param value1 top level Option
     * @param value2 Nested Option
     * @param value3 Nested Option
     * @param yieldingFunction Generates a result per combination
     * @return Option with a combined value generated by the yielding function
     */
    public static <T1, T2, R1, R2, R> Option<R> forEach3(Option<? extends T1> value1,
                                                         Function<? super T1, ? extends Option<R1>> value2,
                                                         BiFunction<? super T1, ? super R1, ? extends Option<R2>> value3,
                                                         Fn3<? super T1, ? super R1, ? super R2, ? extends R> yieldingFunction) {

        return value1.bind(in -> {

            Option<R1> a = value2.apply(in);
            return a.bind(ina -> {
                Option<R2> b = value3.apply(in,ina);
                return b.map(in2 -> yieldingFunction.apply(in, ina, in2));
            });


        });

    }

    /**
     *
     * Perform a For Comprehension over a Option, accepting 2 generating function.
     * This results in a three level nested internal iteration over the provided Options.
     *
     * <pre>
     * {@code
     *
     *  import static com.aol.cyclops2.reactor.Options.forEach3;
     *
     *  forEach3(Option.just(1),
    a-> Option.just(a+1),
    (a,b) -> Option.<Integer>just(a+b),
    (a,b,c) -> a+b+c <100,
    Tuple::tuple);
     *
     * }
     * </pre>
     *
     * @param value1 top level Option
     * @param value2 Nested Option
     * @param value3 Nested Option
     * @param filterFunction A filtering function, keeps values where the predicate holds
     * @param yieldingFunction Generates a result per combination
     * @return Option with a combined value generated by the yielding function
     */
    public static <T1, T2, R1, R2, R> Option<R> forEach3(Option<? extends T1> value1,
                                                         Function<? super T1, ? extends Option<R1>> value2,
                                                         BiFunction<? super T1, ? super R1, ? extends Option<R2>> value3,
                                                         Fn3<? super T1, ? super R1, ? super R2, Boolean> filterFunction,
                                                         Fn3<? super T1, ? super R1, ? super R2, ? extends R> yieldingFunction) {

        return value1.bind(in -> {

            Option<R1> a = value2.apply(in);
            return a.bind(ina -> {
                Option<R2> b = value3.apply(in,ina);
                return b.filter(in2->filterFunction.apply(in,ina,in2))
                        .map(in2 -> yieldingFunction.apply(in, ina, in2));
            });



        });

    }

    /**
     * Perform a For Comprehension over a Option, accepting a generating function.
     * This results in a two level nested internal iteration over the provided Options.
     *
     *  <pre>
     * {@code
     *
     *   import static com.aol.cyclops2.reactor.Options.forEach;
     *
    forEach(Option.just(1),
    a-> Option.just(a+1),
    Tuple::tuple)
     *
     * }
     * </pre>
     *
     * @param value1 top level Option
     * @param value2 Nested Option
     * @param yieldingFunction Generates a result per combination
     * @return Option with a combined value generated by the yielding function
     */
    public static <T, R1, R> Option<R> forEach2(Option<? extends T> value1, Function<? super T, Option<R1>> value2,
                                                BiFunction<? super T, ? super R1, ? extends R> yieldingFunction) {

        return value1.bind(in -> {

            Option<R1> a = value2.apply(in);
            return a.map(in2 -> yieldingFunction.apply(in,  in2));
        });



    }

    /**
     *
     * Perform a For Comprehension over a Option, accepting a generating function.
     * This results in a two level nested internal iteration over the provided Options.
     *
     * <pre>
     * {@code
     *
     *  import static com.aol.cyclops2.reactor.Options.forEach;
     *
     *  forEach(Option.just(1),
    a-> Option.just(a+1),
    (a,b) -> Option.<Integer>just(a+b),
    (a,b,c) -> a+b+c <100,
    Tuple::tuple);
     *
     * }
     * </pre>
     *
     * @param value1 top level Option
     * @param value2 Nested Option
     * @param filterFunction A filtering function, keeps values where the predicate holds
     * @param yieldingFunction Generates a result per combination
     * @return Option with a combined value generated by the yielding function
     */
    public static <T, R1, R> Option<R> forEach2(Option<? extends T> value1, Function<? super T, ? extends Option<R1>> value2,
                                                BiFunction<? super T, ? super R1, Boolean> filterFunction,
                                                BiFunction<? super T, ? super R1, ? extends R> yieldingFunction) {

        return value1.bind(in -> {

            Option<R1> a = value2.apply(in);
            return a.filter(in2->filterFunction.apply(in,in2))
                    .map(in2 -> yieldingFunction.apply(in,  in2));
        });




    }


    public static Option<Double> optional(OptionalDouble d){
        return d.isPresent() ? Option.some(d.getAsDouble()) : Option.none();
    }
    public static Option<Long> optional(OptionalLong l){
        return l.isPresent() ? Option.some(l.getAsLong()) : Option.none();
    }
    public static Option<Integer> optional(OptionalInt l){
        return l.isPresent() ? Option.some(l.getAsInt()) : Option.none();
    }

    /**
     * Sequence operation, take a Collection of Options and turn it into a Option with a Collection
     * By constrast with {@link Options#sequencePresent(CollectionX)}, if any Options are empty the result
     * is an empty Option
     *
     * <pre>
     * {@code
     *
     *  Option<Integer> just = Option.of(10);
    Option<Integer> none = Option.empty();
     *
     *  Option<ListX<Integer>> opts = Options.sequence(ListX.of(just, none, Option.of(1)));
    //Option.empty();
     *
     * }
     * </pre>
     *
     *
     * @param opts Maybes to Sequence
     * @return  Maybe with a List of values
     */
    public static <T> Option<ListX<T>> sequence(final CollectionX<Option<T>> opts) {
        return sequence(opts.stream()).map(s -> s.toListX());

    }
    /**
     * Sequence operation, take a Collection of Options and turn it into a Option with a Collection
     * Only successes are retained. By constrast with {@link Options#sequence(CollectionX)} Option#empty types are
     * tolerated and ignored.
     *
     * <pre>
     * {@code
     *  Option<Integer> just = Option.of(10);
    Option<Integer> none = Option.empty();
     *
     * Option<ListX<Integer>> maybes = Options.sequencePresent(ListX.of(just, none, Option.of(1)));
    //Option.of(ListX.of(10, 1));
     * }
     * </pre>
     *
     * @param opts Options to Sequence
     * @return Option with a List of values
     */
    public static <T> Option<ListX<T>> sequencePresent(final CollectionX<Option<T>> opts) {
        return sequence(opts.stream().filter(Option::isSome)).map(s->s.toListX());
    }
    /**
     * Sequence operation, take a Collection of Options and turn it into a Option with a Collection
     * By constrast with {@link Options#sequencePresent(CollectionX)} if any Option types are empty
     * the return type will be an empty Option
     *
     * <pre>
     * {@code
     *
     *  Option<Integer> just = Option.of(10);
    Option<Integer> none = Option.empty();
     *
     *  Option<ListX<Integer>> maybes = Options.sequence(ListX.of(just, none, Option.of(1)));
    //Option.empty();
     *
     * }
     * </pre>
     *
     *
     * @param opts Maybes to Sequence
     * @return  Option with a List of values
     */
    public static <T> Option<ReactiveSeq<T>> sequence(final Stream<Option<T>> opts) {
        return AnyM.sequence(opts.map(Options::anyM), option.INSTANCE)
                .map(ReactiveSeq::fromStream)
                .to(FJWitness::option);

    }
    /**
     * Accummulating operation using the supplied Reducer (@see cyclops2.Reducers). A typical use case is to accumulate into a Persistent Collection type.
     * Accumulates the present results, ignores empty Options.
     *
     * <pre>
     * {@code
     *  Option<Integer> just = Option.of(10);
    Option<Integer> none = Option.empty();

     * Option<PSetX<Integer>> opts = Option.accumulateJust(ListX.of(just, none, Option.of(1)), Reducers.toPSetX());
    //Option.of(PSetX.of(10, 1)));
     *
     * }
     * </pre>
     *
     * @param optionals Options to accumulate
     * @param reducer Reducer to accumulate values with
     * @return Option with reduced value
     */
    public static <T, R> Option<R> accumulatePresent(final CollectionX<Option<T>> optionals, final Reducer<R> reducer) {
        return sequencePresent(optionals).map(s -> s.mapReduce(reducer));
    }
    /**
     * Accumulate the results only from those Options which have a value present, using the supplied mapping function to
     * convert the data from each Option before reducing them using the supplied Monoid (a combining BiFunction/BinaryOperator and identity element that takes two
     * input values of the same type and returns the combined result) {@see cyclops2.Monoids }.
     *
     * <pre>
     * {@code
     *  Option<Integer> just = Option.of(10);
    Option<Integer> none = Option.empty();

     *  Option<String> opts = Option.accumulateJust(ListX.of(just, none, Option.of(1)), i -> "" + i,
    Monoids.stringConcat);
    //Option.of("101")
     *
     * }
     * </pre>
     *
     * @param optionals Options to accumulate
     * @param mapper Mapping function to be applied to the result of each Option
     * @param reducer Monoid to combine values from each Option
     * @return Option with reduced value
     */
    public static <T, R> Option<R> accumulatePresent(final CollectionX<Option<T>> optionals, final Function<? super T, R> mapper,
                                                     final Monoid<R> reducer) {
        return sequencePresent(optionals).map(s -> s.map(mapper)
                .reduce(reducer));
    }
    /**
     * Accumulate the results only from those Options which have a value present, using the
     * supplied Monoid (a combining BiFunction/BinaryOperator and identity element that takes two
     * input values of the same type and returns the combined result) {@see cyclops2.Monoids }.
     *
     * <pre>
     * {@code
     *  Option<Integer> just = Option.of(10);
    Option<Integer> none = Option.empty();

     *  Option<String> opts = Option.accumulateJust(Monoids.stringConcat,ListX.of(just, none, Option.of(1)),
    );
    //Option.of("101")
     *
     * }
     * </pre>
     *
     * @param optionals Options to accumulate
     * @param reducer Monoid to combine values from each Option
     * @return Option with reduced value
     */
    public static <T> Option<T> accumulatePresent(final Monoid<T> reducer, final CollectionX<Option<T>> optionals) {
        return sequencePresent(optionals).map(s -> s
                .reduce(reducer));
    }

    /**
     * Combine an Option with the provided value using the supplied BiFunction
     *
     * <pre>
     * {@code
     *  Options.combine(Option.of(10),Maybe.just(20), this::add)
     *  //Option[30]
     *
     *  private int add(int a, int b) {
    return a + b;
    }
     *
     * }
     * </pre>
     * @param f Option to combine with a value
     * @param v Value to combine
     * @param fn Combining function
     * @return Option combined with supplied value
     */
    public static <T1, T2, R> Option<R> combine(final Option<? extends T1> f, final Value<? extends T2> v,
                                                final BiFunction<? super T1, ? super T2, ? extends R> fn) {
        return narrow(FromCyclopsReact.option(ToCyclopsReact.maybe(f)
                .combine(v, fn)));
    }
    /**
     * Combine an Option with the provided Option using the supplied BiFunction
     *
     * <pre>
     * {@code
     *  Options.combine(Option.of(10),Option.of(20), this::add)
     *  //Option[30]
     *
     *  private int add(int a, int b) {
    return a + b;
    }
     *
     * }
     * </pre>
     *
     * @param f Option to combine with a value
     * @param v Option to combine
     * @param fn Combining function
     * @return Option combined with supplied value, or empty Option if no value present
     */
    public static <T1, T2, R> Option<R> combine(final Option<? extends T1> f, final Option<? extends T2> v,
                                                final BiFunction<? super T1, ? super T2, ? extends R> fn) {
        return combine(f,ToCyclopsReact.maybe(v),fn);
    }

    /**
     * Combine an Option with the provided Iterable (selecting one element if present) using the supplied BiFunction
     * <pre>
     * {@code
     *  Options.zip(Option.of(10),Arrays.asList(20), this::add)
     *  //Option[30]
     *
     *  private int add(int a, int b) {
    return a + b;
    }
     *
     * }
     * </pre>
     * @param f Option to combine with first element in Iterable (if present)
     * @param v Iterable to combine
     * @param fn Combining function
     * @return Option combined with supplied Iterable, or empty Option if no value present
     */
    public static <T1, T2, R> Option<R> zip(final Option<? extends T1> f, final Iterable<? extends T2> v,
                                            final BiFunction<? super T1, ? super T2, ? extends R> fn) {
        return narrow(FromCyclopsReact.option(ToCyclopsReact.maybe(f)
                .zip(v, fn)));
    }

    /**
     * Combine an Option with the provided Publisher (selecting one element if present) using the supplied BiFunction
     * <pre>
     * {@code
     *  Options.zip(Flux.just(10),Option.of(10), this::add)
     *  //Option[30]
     *
     *  private int add(int a, int b) {
    return a + b;
    }
     *
     * }
     * </pre>
     *
     * @param p Publisher to combine
     * @param f  Option to combine with
     * @param fn Combining function
     * @return Option combined with supplied Publisher, or empty Option if no value present
     */
    public static <T1, T2, R> Option<R> zip(final Publisher<? extends T2> p, final Option<? extends T1> f,
                                            final BiFunction<? super T1, ? super T2, ? extends R> fn) {
        return narrow(FromCyclopsReact.option(ToCyclopsReact.maybe(f)
                .zipP(p, fn)));
    }
    /**
     * Narrow covariant type parameter
     *
     * @param optional Option with covariant type parameter
     * @return Narrowed Option
     */
    public static <T> Option<T> narrow(final Option<? extends T> optional) {
        return (Option<T>) optional;
    }

    /**
     * Companion class for creating Type Class instances for working with Options
     * @author johnmcclean
     *
     */
    @UtilityClass
    public static class Instances {


        /**
         *
         * Transform a option, mulitplying every element by 2
         *
         * <pre>
         * {@code
         *  OptionKind<Integer> option = Options.functor()
         *                                      .map(i->i*2, OptionKind.widen(Option.some(1));
         *
         *  //[2]
         *
         *
         * }
         * </pre>
         *
         * An example fluent api working with Options
         * <pre>
         * {@code
         *   OptionKind<Integer> option = Options.unit()
        .unit("hello")
        .then(h->Options.functor().map((String v) ->v.length(), h))
        .convert(OptionKind::narrowK);
         *
         * }
         * </pre>
         *
         *
         * @return A functor for Options
         */
        public static <T,R>Functor<OptionKind.µ> functor(){
            BiFunction<OptionKind<T>,Function<? super T, ? extends R>,OptionKind<R>> map = Instances::map;
            return General.functor(map);
        }
        /**
         * <pre>
         * {@code
         * OptionKind<String> option = Options.unit()
        .unit("hello")
        .convert(OptionKind::narrowK);

        //Option.some("hello"))
         *
         * }
         * </pre>
         *
         *
         * @return A factory for Options
         */
        public static <T> Pure<OptionKind.µ> unit(){
            return General.<OptionKind.µ,T>unit(Instances::of);
        }
        /**
         *
         * <pre>
         * {@code
         * import static com.aol.cyclops.hkt.jdk.OptionKind.widen;
         * import static com.aol.cyclops.util.function.Lambda.l1;
         *
         *
        Options.applicative()
        .ap(widen(Option.some(l1(this::multiplyByTwo))),widen(Option.some(1)));
         *
         * //[2]
         * }
         * </pre>
         *
         *
         * Example fluent API
         * <pre>
         * {@code
         * OptionKind<Function<Integer,Integer>> optionFn =Options.unit()
         *                                                  .unit(Lambda.l1((Integer i) ->i*2))
         *                                                  .convert(OptionKind::narrowK);

        OptionKind<Integer> option = Options.unit()
        .unit("hello")
        .then(h->Options.functor().map((String v) ->v.length(), h))
        .then(h->Options.applicative().ap(optionFn, h))
        .convert(OptionKind::narrowK);

        //Arrays.asOption("hello".length()*2))
         *
         * }
         * </pre>
         *
         *
         * @return A zipper for Options
         */
        public static <T,R> Applicative<OptionKind.µ> applicative(){
            BiFunction<OptionKind< Function<T, R>>,OptionKind<T>,OptionKind<R>> ap = Instances::ap;
            return General.applicative(functor(), unit(), ap);
        }
        /**
         *
         * <pre>
         * {@code
         * import static com.aol.cyclops.hkt.jdk.OptionKind.widen;
         * OptionKind<Integer> option  = Options.monad()
        .flatMap(i->widen(OptionX.range(0,i)), widen(Option.some(1,2,3)))
        .convert(OptionKind::narrowK);
         * }
         * </pre>
         *
         * Example fluent API
         * <pre>
         * {@code
         *    OptionKind<Integer> option = Options.unit()
        .unit("hello")
        .then(h->Options.monad().flatMap((String v) ->Options.unit().unit(v.length()), h))
        .convert(OptionKind::narrowK);

        //Arrays.asOption("hello".length())
         *
         * }
         * </pre>
         *
         * @return Type class with monad functions for Options
         */
        public static <T,R> Monad<OptionKind.µ> monad(){

            BiFunction<Higher<OptionKind.µ,T>,Function<? super T, ? extends Higher<OptionKind.µ,R>>,Higher<OptionKind.µ,R>> flatMap = Instances::flatMap;
            return General.monad(applicative(), flatMap);
        }
        /**
         *
         * <pre>
         * {@code
         *  OptionKind<String> option = Options.unit()
        .unit("hello")
        .then(h->Options.monadZero().filter((String t)->t.startsWith("he"), h))
        .convert(OptionKind::narrowK);

        //Arrays.asOption("hello"));
         *
         * }
         * </pre>
         *
         *
         * @return A filterable monad (with default value)
         */
        public static <T,R> MonadZero<OptionKind.µ> monadZero(){

            return General.monadZero(monad(), OptionKind.empty());
        }
        /**
         * <pre>
         * {@code
         *  OptionKind<Integer> option = Options.<Integer>monadPlus()
        .plus(OptionKind.widen(Arrays.asOption()), OptionKind.widen(Arrays.asOption(10)))
        .convert(OptionKind::narrowK);
        //Arrays.asOption(10))
         *
         * }
         * </pre>
         * @return Type class for combining Options by concatenation
         */
        public static <T> MonadPlus<OptionKind.µ> monadPlus(){
            Monoid<Option<T>> mn = Monoid.of(Option.none(), (a, b) -> a.isSome() ? a : b);
            Monoid<OptionKind<T>> m = Monoid.of(OptionKind.widen(mn.zero()), (f, g)-> OptionKind.widen(
                    mn.apply(OptionKind.narrow(f), OptionKind.narrow(g))));

            Monoid<Higher<OptionKind.µ,T>> m2= (Monoid)m;
            return General.monadPlus(monadZero(),m2);
        }
        /**
         *
         * <pre>
         * {@code
         *  Monoid<OptionKind<Integer>> m = Monoid.of(OptionKind.widen(Arrays.asOption()), (a,b)->a.isEmpty() ? b : a);
        OptionKind<Integer> option = Options.<Integer>monadPlus(m)
        .plus(OptionKind.widen(Arrays.asOption(5)), OptionKind.widen(Arrays.asOption(10)))
        .convert(OptionKind::narrowK);
        //Arrays.asOption(5))
         *
         * }
         * </pre>
         *
         * @param m Monoid to use for combining Options
         * @return Type class for combining Options
         */
        public static <T> MonadPlus<OptionKind.µ> monadPlus(Monoid<OptionKind<T>> m){
            Monoid<Higher<OptionKind.µ,T>> m2= (Monoid)m;
            return General.monadPlus(monadZero(),m2);
        }

        /**
         * @return Type class for traversables with traverse / sequence operations
         */
        public static <C2,T> Traverse<OptionKind.µ> traverse(){

            return General.traverseByTraverse(applicative(), Instances::traverseA);
        }

        /**
         *
         * <pre>
         * {@code
         * int sum  = Options.foldable()
        .foldLeft(0, (a,b)->a+b, OptionKind.widen(Option.some(2)));

        //2
         *
         * }
         * </pre>
         *
         *
         * @return Type class for folding / reduction operations
         */
        public static <T> Foldable<OptionKind.µ> foldable(){
            BiFunction<Monoid<T>,Higher<OptionKind.µ,T>,T> foldRightFn =  (m, l)-> OptionKind.narrow(l).orSome(m.zero());
            BiFunction<Monoid<T>,Higher<OptionKind.µ,T>,T> foldLeftFn = (m, l)-> OptionKind.narrow(l).orSome(m.zero());
            return General.foldable(foldRightFn, foldLeftFn);
        }
        public static <T> Comonad<OptionKind.µ> comonad(){
            Function<? super Higher<OptionKind.µ, T>, ? extends T> extractFn = maybe -> maybe.convert(OptionKind::narrow).some();
            return General.comonad(functor(), unit(), extractFn);
        }

        private <T> OptionKind<T> of(T value){
            return OptionKind.widen(Option.some(value));
        }
        private static <T,R> OptionKind<R> ap(OptionKind<Function< T, R>> lt, OptionKind<T> option){

            Maybe<R> mb = FJ.maybe(lt.narrow()).combine(FJ.maybe(option.narrow()),
                    (a,b)->a.apply(b));
            return OptionKind.widen(mb);

        }
        private static <T,R> Higher<OptionKind.µ,R> flatMap(Higher<OptionKind.µ,T> lt, Function<? super T, ? extends  Higher<OptionKind.µ,R>> fn){
            return OptionKind.widen(OptionKind.narrow(lt).bind(in->fn.andThen(OptionKind::narrow).apply(in)));
        }
        private static <T,R> OptionKind<R> map(OptionKind<T> lt, Function<? super T, ? extends R> fn){

            return OptionKind.widen(OptionKind.narrow(lt).map(t->fn.apply(t)));
        }


        private static <C2,T,R> Higher<C2, Higher<OptionKind.µ, R>> traverseA(Applicative<C2> applicative, Function<? super T, ? extends Higher<C2, R>> fn,
                                                                              Higher<OptionKind.µ, T> ds){
            Option<T> opt = OptionKind.narrow(ds);
            return opt.isSome()?   applicative.map(OptionKind::of, fn.apply(opt.some())) :
                    applicative.unit(OptionKind.empty());
        }

    }


}
