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
package org.neo4j.kernel.impl.skip.base;

import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.helpers.Function2;
import org.neo4j.helpers.Predicate;
import org.neo4j.kernel.impl.skip.LevelGenerator;
import org.neo4j.kernel.impl.skip.SkipListCabinet;
import org.neo4j.kernel.impl.skip.SkipListCabinetProvider;
import org.neo4j.kernel.impl.skip.SkipListOperations;

/**
 * Implementation of {@link org.neo4j.kernel.impl.skip.SkipListOperations} over arbitrary
 * {@link org.neo4j.kernel.impl.skip.SkipListCabinet}, keys, and values
 *
 * Public methods that take a {@link org.neo4j.kernel.impl.skip.SkipListCabinet} argument,
 * will open the default cabinet if called with null
 *
 * Private methods that take a {@link org.neo4j.kernel.impl.skip.SkipListCabinet} argument and whose name ends
 * with an underscore expect that the provided cabinet is not null and open
 */
public abstract class SkipListAccessorBase<R, K, V>
        implements SkipListOperations<R,K,V>, SkipListCabinetProvider<R, K, V>
{
    private final SkipListCabinetProvider<R, K, V> cabinetProvider;

    public SkipListAccessorBase( SkipListCabinetProvider<R, K, V> cabinetProvider ) {
        this.cabinetProvider = cabinetProvider;
    }

    public SkipListCabinet<R, K, V> openCabinet( LevelGenerator levelGenerator ) {
        return cabinetProvider.openCabinet( levelGenerator );
    }

    @Override
    public Function2<SkipListCabinet<R, K, V>, R, R> returnRecords() {
        return SkipListIterator.returnRecords();
    }

    @Override
    public Function2<SkipListCabinet<R, K, V>, R, V> returnValues() {
        return SkipListIterator.returnValues();
    }

    @Override
    public R insertIfMissing( SkipListCabinet<R, K, V> cabinet, K key, V value )
    {
        cabinet.acquire();
        try
        {
            // find best match
            R[] visited = cabinet.newVisitationArray();
            R pred      = findPred( cabinet, key, value, visited );
            R next      = cabinet.getNext( pred, 0 );

            boolean isMissing = ( cabinet.isNil( next ) || 1 == compareRecord_( cabinet, next, key, value ) );
            return isMissing ? insertRecord_( cabinet, key, value, visited ) : next;
        }
        finally
        {
            cabinet.release();
        }
    }

    @Override
    public R removeFirst( SkipListCabinet<R, K, V> cabinet, K key, V value )
    {
        cabinet.acquire();
        try
        {
            // find best match
            R[] visited = cabinet.newVisitationArray();
            R pred      = findPred( cabinet, key, value, visited );
            R next      = cabinet.getNext( pred, 0 );

            boolean isMissing = ( cabinet.isNil( next ) || 1 == compareRecord_( cabinet, next, key, value ) );
            return isMissing ? cabinet.nil() : removeRecord_( cabinet, next, visited );
        }
        finally
        {
            cabinet.release();
        }
    }

    @Override
    public R removeFirst( SkipListCabinet<R, K, V> cabinet, K key )
    {
        cabinet.acquire();
        try
        {
            // find best match
            R[] visited = cabinet.newVisitationArray();
            R pred      = findPred( cabinet, key, visited );
            R next      = cabinet.getNext( pred, 0 );

            boolean isMissing = cabinet.isNil( next );
            return isMissing ? cabinet.nil() : removeRecord_( cabinet, next, visited );
        }
        finally
        {
            cabinet.release();
        }
    }

    @Override
    public boolean removeAll( SkipListCabinet<R, K, V> cabinet, K key )
    {
        cabinet.acquire();
        try
        {
            // TODO: Implement without finding again and again
            boolean removed = false;
            while (! cabinet.isNil( removeFirst( cabinet, key ) ) )
                removed = true;
            return removed;
        }
        finally {
            cabinet.release();
        }
    }

    private R insertRecord_( SkipListCabinet<R, K, V> cabinet, K key, V value, R[] visited )
    {
        // insert new after visited
        int maxLevel = cabinet.getMaxLevel( cabinet.getHead() );
        int newLevel = cabinet.newRandomLevel();
        R entry = cabinet.createRecord( newLevel + 1, key, value ) ;
        // ensure head will be updated if we create a record higher than any other created before
        if ( newLevel > maxLevel )
            for ( int i = maxLevel + 1; i <= newLevel; i++ )
                visited[i] = cabinet.getHead();
        // update linked lists
        for ( int i = 0; i <= newLevel; i++ )
        {
            R visitedNext = cabinet.getNext( visited[i], i );
            cabinet.setNext( entry, i, visitedNext );
            cabinet.setNext( visited[i], i, entry );
        }
        // return inserted record
        return entry;
    }

    private R removeRecord_( SkipListCabinet<R, K, V> cabinet, R removed, R[] visited )
    {
        // update linked lists
        for (int i = 0; i < cabinet.getMaxHeight(); i ++)
        {
            R record  = visited[i];
            R forward = cabinet.getNext( record, i );
            if ( ! cabinet.areSameRecord( removed, forward ) )
                break;
            cabinet.setNext( visited[i], i, cabinet.getNext( removed, i ) );
        }
        // remove actual record
        cabinet.removeRecord( removed );

        return removed;
    }

    @Override
    public boolean contains( SkipListCabinet<R, K, V> cabinet, K key, V value )
    {
        cabinet.acquire();
        try
        {
            return ! cabinet.isNil( findFirst( cabinet, key, value ) );
        }
        finally
        {
            cabinet.release();
        }
    }

    @Override
    public boolean contains( SkipListCabinet<R, K, V> cabinet, K key )
    {
        cabinet.acquire();
        try
        {
            return ! cabinet.isNil( findFirst( cabinet, key ) );
        }
        finally
        {
            cabinet.release();
        }
    }

    @Override
    public <I> ResourceIterator<I> findAll( final SkipListCabinet<R, K, V> cabinet,
                                            Function2<SkipListCabinet<R, K, V>, R, I> resultFun,
                                            final K key,
                                            final V value
                                          )
    {
        // released by iterator
        cabinet.acquire();
        final R next = findFirst( cabinet, key );
        return new SkipListIterator<R, K, V, I>( cabinet, next, new Predicate<R>() {
            @Override
            public boolean accept( R record )
            {
                return ! cabinet.isNil( record ) && 0 == compareRecord_( cabinet, record, key, value );
            }
        }, resultFun);
    }

    @Override
    public <I> ResourceIterator<I> findAll( final SkipListCabinet<R, K, V> cabinet,
                                            Function2<SkipListCabinet<R, K, V>, R, I> resultFun,
                                            final K key
                                          )
    {
        // released by iterator
        cabinet.acquire();
        final R next = findFirst( cabinet, key );
        return new SkipListIterator<R, K, V, I>( cabinet, next, new Predicate<R>() {
            @Override
            public boolean accept( R record )
            {
                return ! cabinet.isNil( record ) && 0 == compareRecordKey_( cabinet, record, key );
            }
        }, resultFun);
    }

    /**
     * Find and return any record with given key and value if it exists, return nil if it doesn't
     */
    @Override
    @SuppressWarnings("unchecked")
    public R findFirst( SkipListCabinet<R, K, V> cabinet, K key, V value ) {
        cabinet.acquire();
        try
        {
            R entry           = cabinet.getHead();
            R next            = cabinet.nil();
            boolean nextIsNil = true;
            for ( int level = cabinet.getMaxLevel( entry ); level >= 0; level-- )
            {
                forward: while (true)
                {
                    next = cabinet.getNext( entry, level );
                    if ( nextIsNil = cabinet.isNil( next ) )
                        break forward;
                    switch ( compareRecord_( cabinet, next, key, value ) )
                    {
                        case -1:
                            entry = next;
                            break;
                        case 0:
                            /* optimization: early exit if we find an equal record before reaching the bottom */
                            return next;
                        case +1:
                            break forward;
                    }
                }
            }
            if ( nextIsNil )
                return next;
            else
                return 0 == compareRecord_( cabinet, next, key, value ) ? next : cabinet.nil();
        }
        finally {
            cabinet.release();
        }
    }

    /**
     * Find smallest record with given key if it exists or return nil if it doesn't
     */
    @Override
    public R findFirst( SkipListCabinet<R, K, V> cabinet, K key )
    {
        cabinet.acquire();
        try
        {
            R entry = cabinet.getHead();
            R next  = cabinet.nil();
            boolean nextIsNil = true;
            for ( int level = cabinet.getMaxLevel( entry ); level >= 0; level-- )
            {
                forward: while (true)
                {
                    next = cabinet.getNext( entry, level );
                    if ( nextIsNil = cabinet.isNil( next ) )
                        break forward;
                    switch ( compareRecordKey_( cabinet, next, key ) )
                    {
                        case -1:
                            entry = next;
                            break;
                        case 0:
                            /* no early exit here, need to walk complete chain to find largest less than (key, _) */
                        case +1:
                            break forward;
                    }
                }
            }
            if ( nextIsNil )
                return next;
            else
                return 0 == compareRecordKey_( cabinet, next, key ) ? next : cabinet.nil();
        }
        finally
        {
            cabinet.release();
        }
    }

    /**
     * Find and return largest record smaller than (key, value) or head if the cabinet is empty
     * and update visited to contain the sequence of visited records (may include head but never nil)
     *
     * @param visited an array of size cabinet.getMaxHeight() or null
     */
    @Override
    @SuppressWarnings("unchecked")
    public R findPred( SkipListCabinet<R, K, V> cabinet, K key, V value, R[] visited ) {
        cabinet.acquire();
        try
        {
            R entry = cabinet.getHead();
            for ( int level = cabinet.getMaxHeight() - 1; level >= 0; level-- )
            {
                forward: while (true)
                {
                    R next = cabinet.getNext( entry, level );
                    if ( cabinet.isNil( next ) )
                        break forward;
                    switch ( compareRecord_( cabinet, next, key, value ) )
                    {
                        case -1:
                            entry = next;
                            break;
                        case 0:
                            /* no early exit here, need to walk complete chain to find largest less than (key, value) */
                        case +1:
                            break forward;
                    }
                }

                if (visited != null)
                    visited[level] = entry;
            }
            return entry;
        }
        finally
        {
            cabinet.release();
        }
    }

    /**
     * Find and return largest record smaller than (key, smallest possible value) or head if the cabinet is empty
     * and update visited to contain the sequence of visited records (may include head but never nil)
     *
     * @param visited an array of size cabinet.getMaxHeight() or null
     */
    @Override
    @SuppressWarnings("unchecked")
    public R findPred( SkipListCabinet<R, K, V> cabinet, K key, R[] visited ) {
        cabinet.acquire();
        try
        {
            R entry = cabinet.getHead();
            for ( int level = cabinet.getMaxHeight() - 1; level >= 0; level-- )
            {
                forward: while (true)
                {
                    R next = cabinet.getNext( entry, level );
                    if ( cabinet.isNil( next ) )
                        break forward;
                    switch ( compareRecordKey_( cabinet, next, key  ) )
                    {
                        case -1:
                            entry = next;
                            break;
                        case 0:
                            /* no early exit here, need to walk complete chain to find largest less than (key, value) */
                        case +1:
                            break forward;
                    }
                }

                if (visited != null)
                    visited[level] = entry;
            }
            return entry;
        }
        finally
        {
            cabinet.release();
        }
    }

    private int compareRecord_( SkipListCabinet<R, K, V> cabinet, R record, K key, V value ) {
        int cmpKeys = compareRecordKey_( cabinet, record, key );
        return 0 == cmpKeys ? compareRecordValue_( cabinet, record, value ) : cmpKeys;
    }

    private int compareRecordKey_( SkipListCabinet<R, K, V> cabinet, R record, K key )
    {
        return Integer.signum( compareKeys( cabinet.getRecordKey( record ), key ) );
    }

    private int compareRecordValue_( SkipListCabinet<R, K, V> cabinet, R record, V value )
    {
        return Integer.signum( compareValues( cabinet.getRecordValue( record ), value ) );
    }
}
