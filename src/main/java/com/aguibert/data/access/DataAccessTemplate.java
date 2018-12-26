/**
 *
 */
package com.aguibert.data.access;

import io.r2dbc.client.R2dbc;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author Andrew
 *
 */
public interface DataAccessTemplate<K, T> {

    public R2dbc getDbc();

    public boolean createDB(String name);

    public boolean autoCreateDB();

    public Mono<Boolean> contains(T item);

    public Mono<Boolean> containsKey(K key);

    public Flux<T> findBy(K key);

    public Flux<Integer> insert(T item);

    public Mono<Boolean> insert(K key, T item);

    public Mono<Long> remove(T item);

    public Flux<Boolean> removeBy(K key);

    public Mono<Long> update(T item, T updatedItem);

    public Mono<Boolean> updateBy(K key, T updatedItem);

}
