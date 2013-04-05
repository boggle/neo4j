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

import static org.neo4j.kernel.impl.skip.store.SkipListStore.HEAD_ID;

import java.nio.ByteBuffer;
import java.util.Collection;

import org.neo4j.kernel.impl.nioneo.store.DynamicRecord;
import org.neo4j.kernel.impl.nioneo.store.InvalidRecordException;
import org.neo4j.kernel.impl.nioneo.store.KeyValueSerialization;
import org.neo4j.kernel.impl.nioneo.store.RecordSerializable;
import org.neo4j.kernel.impl.skip.base.SkipListRecordBase;

public class SkipListStoreRecord<K, V> extends SkipListRecordBase<K, V> implements RecordSerializable
{
    private final SkipListStore<K, V>.StoreView storeView;

    private final long id;
    private final long[] nexts;

    private SkipListRecordState dynState;
    private Collection<DynamicRecord> dynRecords;

    SkipListStoreRecord( SkipListStore<K,V>.StoreView storeView, long id, int maxHeight )
    {
        // use case: directly called to create head
        this( storeView, id, maxHeight, null, null );
    }


    SkipListStoreRecord( SkipListStore<K, V>.StoreView storeView, long id, int height, K key, V value )
    {

        // use case: directly called to create new non-head record
        this( storeView, id, new long[ height ], key, value );
    }

    SkipListStoreRecord( SkipListStore<K, V>.StoreView storeView, long id,
                         ByteBuffer dynBuffer, Collection<DynamicRecord> dynRecords )
    {
        // use case: directly called to de-serialize record
        this( storeView,
              id,
              readNexts( dynBuffer ),
              id == HEAD_ID ? null : storeView.getKeySerializer().deSerialize( dynBuffer ),
              id == HEAD_ID ? null : storeView.getValueSerializer().deSerialize( dynBuffer ),
              SkipListRecordState.LOADED );
        this.dynRecords = dynRecords;
    }

    private SkipListStoreRecord( SkipListStore<K, V>.StoreView storeView, long id, long[] nexts, K key, V value )
    {
        this( storeView, id, nexts, key, value, SkipListRecordState.CREATED );
        this.dynRecords = storeView.allocateDynamicRecords( this );
    }

    private SkipListStoreRecord( SkipListStore<K, V>.StoreView storeView, long id, long[] nexts, K key, V value,
                                 SkipListRecordState dynState )
    {
        super( key, value );
        if ( id < HEAD_ID )
            throw new IllegalArgumentException( "id expected to be >= " + HEAD_ID + " but was: " + id );

        this.storeView = storeView;
        this.id = id;
        this.nexts = nexts;
        this.dynState = dynState;
    }

    @Override
    public int getHeight()
    {
        assertNotRemoved();
        return nexts.length;
    }

    @Override
    public boolean isHead()
    {
        assertNotRemoved();
        return HEAD_ID == id;
    }

    @Override
    public int length()
    {
        assertNotRemoved();
        return 2                /* height */
             + 8 * nexts.length /* next pointers */
             + ( id == HEAD_ID ? 0 : KeyValueSerialization.computeSerializedLength( storeView, key, value ) );
    }

    @Override
    public void serialize( ByteBuffer target )
    {
        assertNotRemoved();
        target.putShort( (short) nexts.length );
        for ( int i = 0; i < nexts.length; i++ )
            target.putLong( nexts[i] );
        if ( ! isHead() )
            KeyValueSerialization.serialize( storeView, key, value, target );
    }

    private static long[] readNexts( ByteBuffer source )
    {
        int length = source.getShort();
        long[] result = new long[length];
        for ( int i = 0; i < length; i++ )
            result[i] = source.getLong();
        return result;
    }

    // Uses by SkipListStore

    void assertNotRemoved()
    {
        if ( SkipListRecordState.REMOVED.equals( dynState ) )
            throw new InvalidRecordException( "Record with id " + id + " already has been removed" );
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

    long getId()
    {
        return id;
    }

    long getNext( int level )
    {
        assertNotRemoved();
        return nexts[level];
    }

    void setNext( int level, long id )
    {
        assertNotRemoved();
        nexts[level] = id;
        dynState = SkipListRecordState.OUTDATED;
    }

    void setRemoved()
    {
        if ( isHead()  )
            throw new IllegalArgumentException( "Cannot remove head record" );

        dynState = SkipListRecordState.REMOVED;
        for ( DynamicRecord dynRecord : dynRecords )
            dynRecord.setInUse( false );
    }

    void write()
    {
        if ( dynState.isOutdated )
            dynRecords = storeView.updateDynamicRecords( this, dynRecords.iterator() );
        if ( dynState.shouldWrite )
            storeView.writeDynamicRecords( dynRecords.iterator() );
        dynState = SkipListRecordState.LOADED;
    }
}
