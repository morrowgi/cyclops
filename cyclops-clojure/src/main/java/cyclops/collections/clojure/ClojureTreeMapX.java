package cyclops.collections.clojure;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.aol.cyclops2.data.collections.extensions.ExtensiblePMapX;
import com.aol.cyclops2.types.Unwrapable;
import com.aol.cyclops2.types.foldable.To;
import com.aol.cyclops2.types.mixins.TupleWrapper;
import cyclops.collections.immutable.PersistentMapX;
import cyclops.companion.MapXs;
import cyclops.control.Eval;
import cyclops.function.Reducer;
import cyclops.stream.ReactiveSeq;
import org.jooq.lambda.tuple.Tuple2;
import org.pcollections.PMap;



import clojure.lang.PersistentTreeMap;
import clojure.lang.PersistentVector;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.experimental.Wither;
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ClojureTreeMapX<K,V> extends AbstractMap<K,V> implements PMap<K,V>, Unwrapable, To<ClojureTreeMapX<K,V>> {
    
    @Wither
    PersistentTreeMap map;
    @Override
    public <R> R unwrap() {
        return (R)map;
    }
    public static <K, V> Reducer<PersistentMapX<K, V>> toPersistentMapX() {
        return Reducer.<PersistentMapX<K, V>> of(empty(), (final PersistentMapX<K, V> a) -> b -> a.plusAll(b), (in) -> {
            final List w = ((TupleWrapper) () -> in).values();
            return singleton((K) w.get(0), (V) w.get(1));
        });
    }
    public static <K, V> Reducer<PersistentMapX<K, V>> toPersistentMapX(@NonNull Comparator<K> comp) {
        return Reducer.<PersistentMapX<K, V>> of(empty(comp), (final PersistentMapX<K, V> a) -> b -> a.plusAll(b), (in) -> {
            final List w = ((TupleWrapper) () -> in).values();
            return singleton(comp,(K) w.get(0), (V) w.get(1));
        });
    }
    public static <K,V> ClojureTreeMapX<K,V> fromMap(PersistentTreeMap map){
        return new ClojureTreeMapX<>(map);
    }
    
    public static <K,V> ClojureTreeMapX<K,V> fromJavaMap(Map<K,V> map){
        PersistentTreeMap res = ( PersistentTreeMap)PersistentTreeMap.create(map);
        return fromMap(res);
    }
    public static <K,V> ClojureTreeMapX<K,V> fromJavaMap(@NonNull Comparator<K> comp, @NonNull Map<K,V> map){
        PersistentTreeMap res = ( PersistentTreeMap)new PersistentTreeMap(null,comp);
        for(Object o : map.entrySet()) {
         Map.Entry e = (Entry) o;
         res = res.assoc(e.getKey(), e.getValue());
        }
        return fromMap(res);
    }
    public static <K,V> PersistentMapX<K,V> empty(){
       return new ExtensiblePMapX<K,V>(fromMap(PersistentTreeMap.EMPTY), Eval.later(()->toPersistentMapX()));
    }
    public static <K,V> PersistentMapX<K,V> empty(@NonNull Comparator<K> comp){
        return new ExtensiblePMapX<K,V>(fromMap(PersistentTreeMap.EMPTY),Eval.later(()->toPersistentMapX(comp)));
     }
    public static <K,V> PMap<K,V> singletonPMap(K key,V value){
        PersistentTreeMap map = ( PersistentTreeMap)PersistentTreeMap.create(MapXs.of(key, value));
        return fromMap(map);
     }
    public static <K,V> PersistentMapX<K,V> singleton(K key,V value){
        PersistentTreeMap map = ( PersistentTreeMap)PersistentTreeMap.create(MapXs.of(key, value));
        return new ExtensiblePMapX<K,V>(fromMap(map),Eval.later(()-> ClojureTreeMapX.<K,V>toPersistentMapX()));
     }
    public static <K,V> PersistentMapX<K,V> singleton(@NonNull Comparator<K> comp,K key,V value){
        PersistentTreeMap map = ( PersistentTreeMap)PersistentTreeMap.create(MapXs.of(key, value));
        return new ExtensiblePMapX<K,V>(fromMap(map),Eval.later(()-> ClojureTreeMapX.<K,V>toPersistentMapX(comp)));//ClojureTreeMapX.<K,V>toPersistentMapX()
     }
    
    
    public static <K,V> PersistentMapX<K,V> fromStream(@NonNull ReactiveSeq<Tuple2<K,V>> stream){
        return stream.mapReduce(toPersistentMapX());
    }
    
    @Override
    public PMap<K, V> plus(K key, V value) {
        return withMap((PersistentTreeMap)map.cons(PersistentVector.create(key,value)));
    }
    @Override
    public PMap<K, V> plusAll(java.util.Map<? extends K, ? extends V> m2) {
        PersistentTreeMap m = map;
        for(Object next : m2.entrySet()){
            m = (PersistentTreeMap)m.cons(next);
        }
        return withMap(m);
    }
    @Override
    public PMap<K, V> minus(Object key) {
      
        
        return withMap((PersistentTreeMap)map.without(key));
     
    }
   
    @Override
    public PMap<K, V> minusAll(Collection<?> keys) {
      
       PersistentTreeMap m = map;
       for(Object key : keys){
          
           m = (PersistentTreeMap)m.without(key);
       }
       return withMap(m);
        
    }
    @Override
    public Set<java.util.Map.Entry<K, V>> entrySet() {
        return map.entrySet();
        
    }
    /* (non-Javadoc)
     * @see java.util.AbstractMap#get(java.lang.Object)
     */
    @Override
    public V get(Object key) {
       return (V)map.valAt(key);
    }

    public static <K,V> PersistentMapX<K,V> copyFromMap(Map<K,V> map){
        return ClojureTreeMapX.<K,V>empty()
                .plusAll(map);
    }

    public static <K,V> PersistentMapX<K,V> copyFromMap(Comparator<K> c,Map<K,V> map){
        return ClojureTreeMapX.<K,V>empty(c)
                .plusAll(map);
    }
   
   
}
