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
package org.neo4j.kernel.impl.skip.store;

import java.util.Collection;

import org.neo4j.kernel.impl.nioneo.store.DynamicRecord;
import org.neo4j.kernel.impl.nioneo.store.InvalidRecordException;
import org.neo4j.kernel.impl.skip.base.SkipListRecordBase;

import static org.neo4j.kernel.impl.skip.store.SkipListStore.HEAD_ID;

public class SkipListStoreRecord<K, V> extends SkipListRecordBase<K, V>
{
    private final long id;
    private final long[] nexts;

    private transient SkipListRecordState recordState;

    SkipListStoreRecord( long id, K key, V value, long[] nexts, SkipListRecordState recordState )
    {
        super( key, value );
        if ( id < HEAD_ID )
            throw new IllegalArgumentException( "id expected to be > " + HEAD_ID + " but was: " + id );

        this.id = id;
        this.nexts = nexts;
        this.recordState = recordState;
    }

    SkipListStoreRecord( long[] nexts, SkipListRecordState recordState )
    {
        super( null, null );
        this.id = HEAD_ID;
        this.nexts = nexts;
        this.recordState = recordState;
    }

    @Override
    public K getKey()
    {
        assertNotRemoved();
        return super.getKey();
    }

    @Override
    public V getValue()
    {
        assertNotRemoved();
        return super.getValue();
    }

    @Override
    public boolean isHead()
    {
        return HEAD_ID == id;
    }

    @Override
    public int getHeight()
    {
        return nexts.length;
    }

    // Uses by SkipListStore

    void assertNotRemoved()
    {
        if ( SkipListRecordState.REMOVED.equals( recordState ) )
            throw new InvalidRecordException( "Record with id " + id + " already has been removed" );
    }

    long getId()
    {
        return id;
    }

    long getNext( int level )
    {
        assertNotRemoved();
        return nexts[ level ];
    }

    void setNext( int level, long id )
    {
        assertNotRemoved();
        nexts[ level ] = id;
        recordState = SkipListRecordState.OUTDATED;
    }

    SkipListRecordState getRecordState()
    {
        return recordState;
    }

    Collection<DynamicRecord> getDynamicRecords()
    {
        // intended, cf. SkipListStoreLoadedRecord
        return null;
    }

    void setRemoved()
    {
        if ( isHead()  )
            throw new IllegalArgumentException( "Cannot remove head record" );

        recordState = SkipListRecordState.REMOVED;
    }
}
