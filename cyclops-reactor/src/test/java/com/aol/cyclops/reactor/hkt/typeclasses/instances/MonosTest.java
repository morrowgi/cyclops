package com.aol.cyclops.reactor.hkt.typeclasses.instances;
import static com.aol.cyclops.reactor.hkt.MonoKind.widen;

import static cyclops.companion.reactor.Monos.Instances.*;
import static cyclops.function.Lambda.l1;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import cyclops.companion.reactor.Monos;
import com.aol.cyclops.reactor.hkt.MonoKind;
import com.aol.cyclops2.hkt.Higher;
import cyclops.async.Future;
import cyclops.companion.reactor.Monos.Instances;
import cyclops.control.Maybe;
import cyclops.function.Fn1;
import cyclops.function.Monoid;
import cyclops.monads.ReactorWitness;
import cyclops.monads.ReactorWitness.mono;
import cyclops.monads.Witness;
import cyclops.monads.Witness.maybe;
import cyclops.typeclasses.monad.Applicative;
import cyclops.typeclasses.monad.Traverse;
import org.junit.Test;


import reactor.core.publisher.Mono;

public class MonosTest {

    @Test
    public void unit(){
        
        MonoKind<String> opt = Instances.unit()
                                            .unit("hello")
                                            .convert(MonoKind::narrowK);
        
        assertThat(opt.toFuture().join(),equalTo(Future.ofResult("hello").join()));
    }
    @Test
    public void functor(){
        
        MonoKind<Integer> opt = Instances.unit()
                                     .unit("hello")
                                     .applyHKT(h-> Instances.functor().map((String v) ->v.length(), h))
                                     .convert(MonoKind::narrowK);
        
        assertThat(opt.toFuture().join(),equalTo(Future.ofResult("hello".length()).join()));
    }
    @Test
    public void apSimple(){
        Instances.applicative()
            .ap(widen(Future.ofResult(l1(this::multiplyByTwo))),widen(Future.ofResult(1)));
    }
    private int multiplyByTwo(int x){
        return x*2;
    }
    @Test
    public void applicative(){
        
        MonoKind<Fn1<Integer,Integer>> optFn = Instances.unit().unit(l1((Integer i) ->i*2)).convert(MonoKind::narrowK);
        
        MonoKind<Integer> opt = Instances.unit()
                                     .unit("hello")
                                     .applyHKT(h-> Instances.functor().map((String v) ->v.length(), h))
                                     .applyHKT(h-> Instances.applicative().ap(optFn, h))
                                     .convert(MonoKind::narrowK);
        
        assertThat(opt.toFuture().join(),equalTo(Future.ofResult("hello".length()*2).join()));
    }
    @Test
    public void monadSimple(){
       MonoKind<Integer> opt  = Instances.monad()
                                            .<Integer,Integer>flatMap(i->widen(Future.ofResult(i*2)), widen(Future.ofResult(3)))
                                            .convert(MonoKind::narrowK);
    }
    @Test
    public void monad(){
        
        MonoKind<Integer> opt = Instances.unit()
                                     .unit("hello")
                                     .applyHKT(h-> Instances.monad().flatMap((String v) -> Instances.unit().unit(v.length()), h))
                                     .convert(MonoKind::narrowK);
        
        assertThat(opt.toFuture().join(),equalTo(Future.ofResult("hello".length()).join()));
    }
    @Test
    public void monadZeroFilter(){
        
        MonoKind<String> opt = Instances.unit()
                                     .unit("hello")
                                     .applyHKT(h-> Instances.monadZero().filter((String t)->t.startsWith("he"), h))
                                     .convert(MonoKind::narrowK);
        
        assertThat(opt.toFuture().join(),equalTo(Future.ofResult("hello").join()));
    }
    @Test
    public void monadZeroFilterOut(){
        
        MonoKind<String> opt = Instances.unit()
                                     .unit("hello")
                                     .applyHKT(h-> Instances.monadZero().filter((String t)->!t.startsWith("he"), h))
                                     .convert(MonoKind::narrowK);
        
        assertTrue(opt.block()==null);
    }
    
    @Test
    public void monadPlus(){
        MonoKind<Integer> opt = Instances.<Integer>monadPlus()
                                      .plus(MonoKind.widen(Mono.empty()), MonoKind.widen(Mono.just(10)))
                                      .convert(MonoKind::narrowK);
        assertTrue(opt.block()==null);
    }
    @Test
    public void monadPlusNonEmpty(){
        
        Monoid<MonoKind<Integer>> m = Monoid.of(MonoKind.widen(Mono.empty()), (a, b)->a.toFuture().isDone() ? b : a);
        MonoKind<Integer> opt = Instances.<Integer>monadPlusK(m)
                                      .plus(MonoKind.widen(Mono.just(5)), MonoKind.widen(Mono.just(10)))
                                      .convert(MonoKind::narrowK);
        assertThat(opt.block(),equalTo(10));
    }
    @Test
    public void  foldLeft(){
        int sum  = Instances.foldable()
                        .foldLeft(0, (a,b)->a+b, MonoKind.widen(Future.ofResult(4)));
        
        assertThat(sum,equalTo(4));
    }
    @Test
    public void  foldRight(){
        int sum  = Instances.foldable()
                        .foldRight(0, (a,b)->a+b, MonoKind.widen(Future.ofResult(1)));
        
        assertThat(sum,equalTo(1));
    }
    @Test
    public void traverse(){
        Traverse<mono> traverse = Monos.Instances.traverse();
        Applicative<maybe> applicative = Maybe.Instances.applicative();

        MonoKind<Integer> mono = MonoKind.widen(Mono.just(1));

        Higher<maybe, Higher<mono, Integer>> t = traverse.traverseA(applicative, (Integer a) -> Maybe.just(a * 2), mono);

        Maybe<Higher<mono, Integer>> res = traverse.traverseA(applicative, (Integer a)-> Maybe.just(a*2),mono)
                                                        .convert(Maybe::narrowK);



       assertThat(res.map(h->h.convert(MonoKind::narrowK).block()),
                  equalTo(Maybe.just(Mono.just(2).block())));
    }
    @Test
    public void sequence(){
        Traverse<mono> traverse = Monos.Instances.traverse();
        Applicative<maybe> applicative = Maybe.Instances.applicative();

        Higher<maybe, Higher<mono, Integer>> res = traverse.sequenceA(applicative, MonoKind.widen(Mono.just(Maybe.just(1))));
        Maybe<Mono<Integer>> nk = res.convert(Maybe::narrowK)
                                     .map(h -> h.convert(MonoKind::narrow));

    }
    
}
