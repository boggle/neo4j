/**
 * Copyright (c) 2002-2013 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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

    R insertAlways( SkipListCabinet<R, K, V> cabinet, K key, V value );

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

    <I> ResourceIterator<I> findAll( SkipListCabinet<R, K, V> cabinetIn,
                                     Function2<SkipListCabinet<R, K, V>, R, I> resultFun  );

    R findFirst( SkipListCabinet<R, K, V> cabinet, K key, V value );

    R findFirst( SkipListCabinet<R, K, V> cabinet, K key );

    R findFirst( SkipListCabinet<R, K, V> cabinet );

    R findPred( SkipListCabinet<R, K, V> cabinet, K key, V value, R[] visited );

    R findPred( SkipListCabinet<R, K, V> cabinet, K key, R[] visited );

    Function2<SkipListCabinet<R, K, V>, R, R> returnRecords();

    Function2<SkipListCabinet<R, K, V>, R, V> returnValues();

    int compareKeys( K a, K b );

    int compareValues( V a, V b );
}
