/*
 * Copyright (C) 2012 Neo Technology
 * All rights reserved
 */
package org.neo4j.kernel.impl.skip;

import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.helpers.Function2;

/**
 * Shared interface of skip lists that store entries sorted by (key, value)
 */
public interface SkipListOperations<R, K, V>
{
    R insertIfMissing( SkipListCabinet<R, K, V> cabinet, K key, V value );

    // R insertAlways( SkipListCabinet<R, K, V> cabinet, K key, V value );

    // R updateOrInsert( SkipListCabinet<R, K, V> cabinet, K key, V value, V newValue );

    // R updateIfFound( SkipListCabinet<R, K, V> cabinet, K key, V value, V newValue );

    R removeFirst( SkipListCabinet<R, K, V> cabinet, K key, V value );

    R removeFirst( SkipListCabinet<R, K, V> cabinet, K key );

    // R removeAll( SkipListCabinet<R, K, V> cabinet, K key, V value );

    boolean removeAll( SkipListCabinet<R, K, V> cabinet, K key );

    boolean contains( SkipListCabinet<R, K, V> cabinet, K key, V value );

    boolean contains( SkipListCabinet<R, K, V> cabinet, K key );

    // int countAll( SkipListOperations<R, K, V> cabinetIn, K key, V value );

    // int countAll( SkipListOperations<R, K, V> cabinetIn, K key );

    <I> ResourceIterator<I> findAll( SkipListCabinet<R, K, V> cabinetIn,
                                     Function2<SkipListCabinet<R, K, V>, R, I> resultFun,  K key, V value );

    <I> ResourceIterator<I> findAll( SkipListCabinet<R, K, V> cabinetIn,
                                     Function2<SkipListCabinet<R, K, V>, R, I> resultFun,  K key );

    R findFirst( SkipListCabinet<R, K, V> cabinet, K key, V value );

    R findFirst( SkipListCabinet<R, K, V> cabinet, K key );

    R findPred( SkipListCabinet<R, K, V> cabinet, K key, V value, R[] visited );

    R findPred( SkipListCabinet<R, K, V> cabinet, K key, R[] visited );

    Function2<SkipListCabinet<R, K, V>, R, R> returnRecords();

    Function2<SkipListCabinet<R, K, V>, R, V> returnValues();

    int compareKeys( K a, K b );

    int compareValues( V a, V b );
}
