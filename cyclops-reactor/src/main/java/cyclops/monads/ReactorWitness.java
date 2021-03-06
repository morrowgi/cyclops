package cyclops.monads;


import com.aol.cyclops.reactor.adapter.FluxAdapter;
import com.aol.cyclops.reactor.adapter.MonoAdapter;
import com.aol.cyclops2.types.extensability.FunctionalAdapter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ReactorWitness {
    public static <T> Flux<T> flux(AnyM<flux,? extends T> anyM){
        return anyM.unwrap();
    }
    public static <T> Mono<T> mono(AnyM<mono,? extends T> anyM){
        return anyM.unwrap();
    }
    static interface FluxWitness<W extends ReactorWitness.FluxWitness<W>>  extends WitnessType<W> {

    }
    public static enum flux implements FluxWitness<ReactorWitness.flux> {
        INSTANCE;

        @Override
        public FunctionalAdapter<flux> adapter() {
            return new FluxAdapter();
        }

    }
    static interface MonoWitness<W extends ReactorWitness.MonoWitness<W>>  extends WitnessType<W> {

    }
    public static enum mono implements MonoWitness<mono> {
        INSTANCE;

        @Override
        public FunctionalAdapter<mono> adapter() {
            return new MonoAdapter();
        }

    }
}
