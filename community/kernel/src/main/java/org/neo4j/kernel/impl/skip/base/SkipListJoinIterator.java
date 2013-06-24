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

import java.util.Collection;
import java.util.NoSuchElementException;

import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.kernel.impl.skip.SkipListCabinet;

public class SkipListJoinIterator<R, K, V> implements ResourceIterator<V>
{
    private final Collection<ResourceIterator<V>> iterators;
    private final SkipListCabinet<R, K, V> cabinet;

    private boolean hasNext = false;
    private V next = null;

    public SkipListJoinIterator( SkipListCabinet<R, K, V> cabinet, Collection<ResourceIterator<V>> iterators )
    {
        this.cabinet = cabinet;
        this.iterators = iterators;
        computeIfHasNext();
    }  @Override
    public boolean hasNext()
    {
        return hasNext;
    }

    @Override
    public V next()
    {
        if ( hasNext )
        {
            V result = next;
            computeIfHasNext();
            return result;
        }
        else
            throw new NoSuchElementException(  );
    }

    private void computeIfHasNext()
    {
        throw new UnsupportedOperationException( );
    }

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
