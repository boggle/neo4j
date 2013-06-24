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

import java.util.NoSuchElementException;

import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.helpers.Function2;
import org.neo4j.helpers.Predicate;
import org.neo4j.kernel.impl.skip.SkipListCabinet;

/**
 * Iterates starting from a given skip list record and continues as long as the given predicate evaluates to true
 *
 * The predicate is expected to guard against nil
 */
public class SkipListIterator<R, K, V, I> implements ResourceIterator<I>
{
    private final SkipListCabinet<R, K, V> cabinet;
    private final Predicate<R> pred;
    private final Function2<SkipListCabinet<R, K, V>, R, I> resultFun;

    private R next;
    private boolean hasNext;

    @SuppressWarnings( "rawtypes" )
    private static final Function2 FUNCTION_RECORDS = new Function2()
    {
        @Override
        public Object apply( Object cabinet, Object entry )
        {
            return entry;
        }
    };
    
    @SuppressWarnings( "unchecked" )
    public static <R, K, V> Function2<SkipListCabinet<R, K, V>, R, R> returnRecords()
    {
        return FUNCTION_RECORDS;
    }
    
    private static final Function2 FUNCTION_RECORD_VALUES = new Function2()
    {
        @SuppressWarnings( { "unchecked", "rawtypes" } )
        @Override
        public Object apply( Object cabinet, Object entry )
        {
            return ((SkipListCabinet) cabinet).getRecordValue( entry );
        }
    };

    @SuppressWarnings( "unchecked" )
    public static <R, K, V> Function2<SkipListCabinet<R, K, V>, R, V> returnValues()
    {
        return FUNCTION_RECORD_VALUES;
    }

    public SkipListIterator( SkipListCabinet<R, K, V> cabinet, R next,
                             Predicate<R> pred,
                             Function2<SkipListCabinet<R, K, V>, R, I> resultFun
    )
    {
        this.cabinet   = cabinet;
        this.pred      = pred;
        this.resultFun = resultFun;

        this.next    = next;
        computeIfHasNext();
    }

    @Override
    public boolean hasNext()
    {
        return hasNext;
    }

    @Override
    public I next()
    {
        if (! hasNext )
            throw new NoSuchElementException(  );

        I result = resultFun.apply( cabinet, next );
        next     = cabinet.getNext( next, 0 );
        computeIfHasNext();
        return result;
    }

    private void computeIfHasNext()
    {
        this.hasNext = pred.accept( next );
        if ( ! hasNext )
        {
            cabinet.release();
        }
    }

    @Override
    public void close()
    {
        if ( hasNext )
        {
            hasNext = false;
            cabinet.release();
        }
    }

    @Override
    public void remove()
    {
        throw new UnsupportedOperationException(  );
    }
}
