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
import org.neo4j.kernel.impl.nioneo.store.KeyValueSerializer;
import org.neo4j.kernel.impl.nioneo.store.RecordSerializable;
import org.neo4j.kernel.impl.skip.base.SkipListRecordBase;

public class SkipListStoreRecord<K, V> extends SkipListRecordBase<K, V> implements RecordSerializable
{
    private final KeyValueSerializer<K, V> kvSerializer;

    final long id;
    final long[] nexts;

    // Used by skip list cabinet

    transient boolean changed = false;
    transient boolean removed = false;
    transient Collection<DynamicRecord> dynRecords;

    SkipListStoreRecord( KeyValueSerializer<K, V> kvSerializer, long id, ByteBuffer source )
    {
        this( kvSerializer, id, readNexts( source ),
                id == HEAD_ID ? null : kvSerializer.getKeySerializer().deSerialize( source ),
                id == HEAD_ID ? null : kvSerializer.getValueSerializer().deSerialize( source ) );
    }

    SkipListStoreRecord( KeyValueSerializer<K, V> kvSerializer, long id, long[] next, K key, V value )
    {
        super( key, value );
        if ( id < HEAD_ID )
            throw new IllegalArgumentException( "id expected to be >= " + HEAD_ID + " but was: " + id );
        this.kvSerializer = kvSerializer;
        this.id = id;
        this.nexts = next;
    }

    SkipListStoreRecord( KeyValueSerializer<K, V> kvSerializer, long id, int height, K key, V value )
    {
        this( kvSerializer, id, new long[height], key, value );
    }

    SkipListStoreRecord( KeyValueSerializer<K, V> kvSerializer, int maxHeight )
    {
        this( kvSerializer, HEAD_ID, maxHeight, null, null );
    }

    @Override
    public int getHeight()
    {
        return nexts.length;
    }

    @Override
    public int length()
    {
        return 2                /* height */
             + 8 * nexts.length /* next pointers */
             + ( id == HEAD_ID ? 0 : KeyValueSerialization.computeSerializedLength( kvSerializer, key, value ) );
    }

    @Override
    public void serialize( ByteBuffer target )
    {
        target.putShort( (short) nexts.length );
        for ( int i = 0; i < nexts.length; i++ )
            target.putLong( nexts[i] );
        if ( id != HEAD_ID )
            KeyValueSerialization.serialize( kvSerializer, key, value, target );
    }

    private static long[] readNexts( ByteBuffer source )
    {
        int length = source.getShort();
        long[] result = new long[length];
        for ( int i = 0; i < length; i++ )
            result[i] = source.getLong();
        return result;
    }

    void assertNotRemoved()
    {
        if ( removed )
            throw new InvalidRecordException( "Record with id " + id + " already has been removed" );
    }
}
