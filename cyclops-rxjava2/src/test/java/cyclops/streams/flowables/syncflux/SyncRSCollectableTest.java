package cyclops.streams.flowables.syncflux;



import cyclops.companion.rx2.Flowables;

import cyclops.streams.flowables.CollectableTest;
import org.jooq.lambda.Collectable;
import reactor.core.publisher.Flux;

public class SyncRSCollectableTest extends CollectableTest {


    public <T> Collectable<T> of(T... values){
        return Flowables.reactiveSeq(Flux.just(values)).collectors();
    }

}
