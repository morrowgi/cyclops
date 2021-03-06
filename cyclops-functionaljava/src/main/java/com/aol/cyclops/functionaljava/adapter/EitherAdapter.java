package com.aol.cyclops.functionaljava.adapter;



import cyclops.companion.functionaljava.Eithers;
import cyclops.monads.FJ;
import cyclops.monads.FJWitness;
import cyclops.monads.FJWitness.either;
import cyclops.conversion.functionaljava.FromCyclopsReact;
import cyclops.conversion.functionaljava.ToCyclopsReact;
import com.aol.cyclops2.types.extensability.AbstractFunctionalAdapter;
import cyclops.control.Xor;
import cyclops.monads.AnyM;

import fj.data.Either;
import lombok.AllArgsConstructor;

import java.util.function.Function;
import java.util.function.Predicate;


@AllArgsConstructor
public class EitherAdapter<L> extends AbstractFunctionalAdapter<either> {



    @Override
    public <T> Iterable<T> toIterable(AnyM<either, T> t) {
        return ToCyclopsReact.xor(either(t));
    }

    @Override
    public <T, R> AnyM<either, R> ap(AnyM<either,? extends Function<? super T,? extends R>> fn, AnyM<either, T> apply) {
        Either<L,T> f = either(apply);
        Either<L,? extends Function<? super T, ? extends R>> fnF = either(fn);
        Either<L,R> res = FromCyclopsReact.either(ToCyclopsReact.xor(fnF).combine(ToCyclopsReact.xor(f), (a, b) -> a.apply(b)));
        return FJ.either(res);

    }

    @Override
    public <T> AnyM<either, T> filter(AnyM<either, T> t, Predicate<? super T> fn) {
        return t;
    }

    <T> Either<L,T> either(AnyM<either,T> anyM){
        return anyM.unwrap();
    }

    @Override
    public <T> AnyM<either, T> empty() {
        return FJ.either(Either.left(null));
    }



    @Override
    public <T, R> AnyM<either, R> flatMap(AnyM<either, T> t,
                                     Function<? super T, ? extends AnyM<either, ? extends R>> fn) {
        return FJ.either(either(t).right().bind(a-> either((AnyM<either,R>)fn.apply(a))));

    }

    @Override
    public <T> AnyM<either, T> unitIterable(Iterable<T> it)  {

        return FJ.either(FromCyclopsReact.either(Xor.fromIterable(it)));
    }

    @Override
    public <T> AnyM<either, T> unit(T o) {
        return FJ.either(Either.right(o));
    }

    @Override
    public <T, R> AnyM<either, R> map(AnyM<either, T> t, Function<? super T, ? extends R> fn) {
        return Eithers.anyM(either(t).bimap(i->i, x->fn.apply(x)));
    }
}
