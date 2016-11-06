package com.aol.cyclops.hkt.instances;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import com.aol.cyclops.Monoid;
import com.aol.cyclops.hkt.alias.Higher;
import com.aol.cyclops.hkt.typeclasses.Unit;
import com.aol.cyclops.hkt.typeclasses.comonad.Comonad;
import com.aol.cyclops.hkt.typeclasses.foldable.Foldable;
import com.aol.cyclops.hkt.typeclasses.functor.Functor;
import com.aol.cyclops.hkt.typeclasses.monad.Applicative;
import com.aol.cyclops.hkt.typeclasses.monad.Monad;
import com.aol.cyclops.hkt.typeclasses.monad.MonadPlus;
import com.aol.cyclops.hkt.typeclasses.monad.MonadZero;
import com.aol.cyclops.hkt.typeclasses.monad.Traverse;
import com.aol.cyclops.hkt.typeclasses.monad.TraverseBySequence;

import lombok.AllArgsConstructor;
import lombok.Builder;

public interface General {
    @AllArgsConstructor
    static class GeneralFunctor<CRE,A,B> implements Functor<CRE>{
      
        BiFunction<? extends Higher<CRE,A>,Function<? super A, ? extends B>,? extends Higher<CRE,B>> mapRef;
        
        
        <T,R> BiFunction<Higher<CRE,T>,Function<? super T, ? extends R>,Higher<CRE,R>> mapRef(){
            return (BiFunction)mapRef;
        }
        
        @Override
        public <T,R> Higher<CRE,R> map(Function<? super T,? extends R> fn, Higher<CRE,T> ds){
            return this.<T,R>mapRef().apply(ds,fn);
        }
    }
    static  <CRE,T,R> GeneralFunctor<CRE,T,R> functor(BiFunction<? extends Higher<CRE,T>,Function<? super T, ? extends R>,? extends Higher<CRE,R>>  f){
    
        return new GeneralFunctor<>(f);
         
    }
    @AllArgsConstructor
    static class GeneralUnit<CRE,A> implements Unit<CRE> {
        
        Function<A,Higher<CRE,A>> unitRef;
       
         <T> Function<T,Higher<CRE,T>> unitRef(){
            return (Function) unitRef;
        }
         @Override
        public <T> Higher<CRE,T> unit(T value){
            return this.<T>unitRef().apply(value);
        }
    }
    static  <CRE,A> GeneralUnit<CRE,A> unit(Function<A,Higher<CRE,A>> unitRef){
   
        return new GeneralUnit<CRE,A>(unitRef);
    }

    @AllArgsConstructor
    @Builder
    static class GeneralApplicative<CRE,A,B> implements Applicative<CRE>{
        Functor<CRE> functor;
        Unit<CRE> unit;
        BiFunction<? extends Higher<CRE, Function<? super A, ? extends B>>,? extends Higher<CRE,A>,? extends Higher<CRE,B>> applyRef;
        
        
        <T,R> BiFunction<Higher<CRE, Function<? super T, ? extends R>>,Higher<CRE,T>,Higher<CRE,R>> applyRef(){
            return (BiFunction)applyRef;
        }
        
        @Override
        public <T,R> Higher<CRE,R> ap(Higher<CRE, Function<? super T, ? extends R>> fn, 
                                    Higher<CRE,T> apply){
          
            return  this.<T,R>applyRef().apply(fn,apply);
        }
      
        @Override
        public <T, R> Higher<CRE, R> map(Function<? super T, ? extends R> fn, Higher<CRE, T> ds) {
            return functor.map(fn, ds);
        }
        @Override
        public <T> Higher<CRE, T> unit(T value) {
           return unit.unit(value);
        }
    }
    
    static  <CRE,T,R> GeneralApplicative<CRE,T,R> applicative(Functor<CRE> functor, Unit<CRE> unit,
            BiFunction<? extends Higher<CRE, Function<? super T, ? extends R>>,? extends Higher<CRE,T>,? extends Higher<CRE,R>> applyRef) {
        
        return new GeneralApplicative<CRE,T,R>(functor,unit,applyRef);
        
    }
    
    @AllArgsConstructor
    @Builder
    static class GeneralMonad<CRE,A,B> implements  Monad<CRE> {
        
       
        Applicative<CRE> applicative;
        BiFunction<? extends Higher<CRE,A>,Function<? super A,? extends Higher<CRE,B>>,? extends Higher<CRE,B>> bindRef; //reference to bind / flatMap method
       
        
        <T,R> BiFunction<Higher<CRE,T>,Function<? super T,? extends Higher<CRE,R>>,Higher<CRE,R>> bindRef(){
            return (BiFunction)bindRef;
        }
        
       
        @Override
        public <T,R> Higher<CRE,R> flatMap(Function<? super T,? extends Higher<CRE,R>> fn,Higher<CRE,T> ds){
            return this.<T,R>bindRef().apply(ds,fn);
        }
       
        
        @Override
        public <T> Higher<CRE,T> unit(T value){
            return applicative.unit(value);
            
        }


        @Override
        public <T, R> Higher<CRE, R> map(Function<? super T, ? extends R> fn, Higher<CRE, T> ds) {
           return applicative.map(fn, ds);
        }


        @Override
        public <T, R> Higher<CRE, R> ap(Higher<CRE, Function<? super T, ? extends R>> fn, Higher<CRE, T> apply) {
            return applicative.ap(fn, apply);
        }
    }
    static  <CRE,A,B> GeneralMonad<CRE,A,B> monad(Applicative<CRE> applicative,
            BiFunction<? extends Higher<CRE,A>,Function<? super A,? extends Higher<CRE,B>>,? extends Higher<CRE,B>> bindRef) {
   
        return new GeneralMonad<CRE,A,B>(applicative,bindRef);
        
    }
    @AllArgsConstructor
    static class GeneralMonadZero<CRE,A,B>  implements MonadZero<CRE>{
        Higher<CRE, A> zero;
        Monad<CRE> monad;
        
        

        @Override
        public <T> Higher<CRE, T> zero() {
            return (Higher)zero;
        }

        @Override
        public <T, R> Higher<CRE, R> flatMap(Function<? super T, ? extends Higher<CRE, R>> fn, Higher<CRE, T> ds) {
            return monad.flatMap(fn, ds);
        }

        @Override
        public <T, R> Higher<CRE, R> ap(Higher<CRE, Function<? super T, ? extends R>> fn, Higher<CRE, T> apply) {
            return monad.ap(fn, apply);
        }

        @Override
        public <T, R> Higher<CRE, R> map(Function<? super T, ? extends R> fn, Higher<CRE, T> ds) {
            return monad.map(fn, ds);
        }

        @Override
        public <T> Higher<CRE, T> unit(T value) {
            return monad.unit(value);
        }
        
    }
    static  <CRE,A,B> GeneralMonadZero<CRE,A,B> monadZero(Monad<CRE> monad,
            Higher<CRE, A> zero) {
   
        return new GeneralMonadZero<CRE,A,B>(zero,monad);
        
    }
    @AllArgsConstructor
    static class GeneralMonadPlus<CRE,T,B> implements MonadPlus<CRE,T>{
        Monoid<Higher<CRE, T>> monoid;
        Monad<CRE> monad;
        

        @Override
        public <T, R> Higher<CRE, R> flatMap(Function<? super T, ? extends Higher<CRE, R>> fn, Higher<CRE, T> ds) {
            return monad.flatMap(fn, ds);
        }

        @Override
        public <T, R> Higher<CRE, R> ap(Higher<CRE, Function<? super T, ? extends R>> fn, Higher<CRE, T> apply) {
            return monad.ap(fn,apply);
        }

        @Override
        public <T, R> Higher<CRE, R> map(Function<? super T, ? extends R> fn, Higher<CRE, T> ds) {
            return monad.map(fn,ds);
        }

        @Override
        public <T> Higher<CRE, T> unit(T value) {
            return monad.unit(value);
        }

        

        @Override
        public Monoid<Higher<CRE, T>> monoid() {
            return (Monoid)monoid;
        }
        
    }
    static  <CRE,A,B> GeneralMonadPlus<CRE,A,B> monadPlus(Monad<CRE> monad,
            Monoid<Higher<CRE, A>> monoid) {
   
        return new GeneralMonadPlus<CRE,A,B>(monoid,monad);
        
    }
    @AllArgsConstructor
    static class GeneralComonad<CRE,A,B> implements Comonad<CRE>{
        GeneralFunctor<CRE,A,B> functor;
        GeneralUnit<CRE,A> unit;
        Function<? super Higher<CRE, A>, ? extends List<A>> extractFn;
        <T> Function<? super Higher<CRE, T>, ? extends List<T>> extractFn(){
            return (Function)extractFn;
        }
        @Override
        public <T> Higher<CRE, T> unit(T value) {
            return unit.unit(value);
        }

        @Override
        public <T, R> Higher<CRE, R> map(Function<? super T, ? extends R> fn, Higher<CRE, T> ds) {
            return functor.map(fn, ds);
        }

       

        @Override
        public <T> List<T> extract(Higher<CRE, T> ds) {
           return this.<T>extractFn().apply(ds);
        }
        
    }
    static  <CRE,T,R> GeneralComonad<CRE,T,R> comonad(GeneralFunctor<CRE,T,R> functor, GeneralUnit<CRE,T> unit,
            Function<? super Higher<CRE, T>, ? extends List<T>> extractFn ) {
        
        return new GeneralComonad<>(functor,unit,extractFn);
        
    }
    @AllArgsConstructor
    static class GeneralFoldable<CRE,T> implements Foldable<CRE>{
        BiFunction<Monoid<T>,Higher<CRE,T>,T> foldRightFn;
        BiFunction<Monoid<T>,Higher<CRE,T>,T> foldLeftFn;
        
        <T> BiFunction<Monoid<T>,Higher<CRE,T>,T> foldRightFn(){
            return (BiFunction)foldRightFn;
        }
        <T> BiFunction<Monoid<T>,Higher<CRE,T>,T> foldLeftFn(){
            return (BiFunction)foldLeftFn;
        }
        
        public <T> T foldRight(Monoid<T> monoid, Higher<CRE,T> ds){
            return this.<T>foldRightFn().apply(monoid,ds);
        }
        
        public <T> T foldLeft(Monoid<T> monoid, Higher<CRE,T> ds){
            return this.<T>foldLeftFn().apply(monoid,ds);
        }
    }
    
    static <CRE,T> GeneralFoldable<CRE,T> foldable(BiFunction<Monoid<T>,Higher<CRE,T>,T> foldRightFn,BiFunction<Monoid<T>,Higher<CRE,T>,T> foldLeftFn){
        return new GeneralFoldable<CRE,T>(foldRightFn,foldLeftFn);
    }
    @AllArgsConstructor
    static class GeneralTraverse<CRE,C2,A,B> implements TraverseBySequence<CRE>{

        Applicative<CRE> applicative;
        BiFunction<Applicative<C2>,Higher<CRE, Higher<C2, A>>,Higher<C2, Higher<CRE, A>> > sequenceFn;
        
        <C2,T> BiFunction<Applicative<C2>,Higher<CRE, Higher<C2, T>>,Higher<C2, Higher<CRE, T>> > sequenceFn(){
            return (BiFunction)sequenceFn;
        }

        @Override
        public <T, R> Higher<CRE, R> ap(Higher<CRE, Function<? super T, ? extends R>> fn, Higher<CRE, T> apply) {
            return applicative.ap(fn, apply);
        }

        @Override
        public <T, R> Higher<CRE, R> map(Function<? super T, ? extends R> fn, Higher<CRE, T> ds) {
            return applicative.map(fn, ds);
        }

        @Override
        public <T> Higher<CRE, T> unit(T value) {
            return applicative.unit(value);
        }

        @Override
        public <C2, T> Higher<C2, Higher<CRE, T>> sequenceA(Applicative<C2> applicative,
                Higher<CRE, Higher<C2, T>> ds) {
           return this.<C2,T>sequenceFn().apply(applicative, ds);
        }
        
    }
    static <CRE,C2,T,R> Traverse<CRE> traverse(Applicative<CRE> applicative,
            BiFunction<Applicative<C2>,Higher<CRE, Higher<C2, T>>,Higher<C2, Higher<CRE, T>> > sequenceFn)  {
        return new GeneralTraverse<>(applicative,sequenceFn);
    }
    

}
