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

import java.io.File;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.neo4j.helpers.ThisShouldNotHappenError;
import org.neo4j.kernel.IdGeneratorFactory;
import org.neo4j.kernel.IdType;
import org.neo4j.kernel.configuration.Config;
import org.neo4j.kernel.impl.nioneo.store.AbstractDynamicStore;
import org.neo4j.kernel.impl.nioneo.store.DynamicRecord;
import org.neo4j.kernel.impl.nioneo.store.FileSystemAbstraction;
import org.neo4j.kernel.impl.nioneo.store.GrowableByteArray;
import org.neo4j.kernel.impl.nioneo.store.InvalidRecordException;
import org.neo4j.kernel.impl.nioneo.store.RecordFieldSerializer;
import org.neo4j.kernel.impl.nioneo.store.windowpool.WindowPoolFactory;
import org.neo4j.kernel.impl.skip.LevelGenerator;
import org.neo4j.kernel.impl.skip.SkipListCabinetProvider;
import org.neo4j.kernel.impl.skip.base.RandomLevelGenerator;
import org.neo4j.kernel.impl.util.StringLogger;

import static org.neo4j.helpers.collection.IteratorUtil.asIterator;

public class SkipListStore<K, V>
        extends AbstractDynamicStore
        implements SkipListCabinetProvider<SkipListStoreRecord<K, V>, K , V>
{
    public static final long HEAD_ID = 1;

    public static final String TYPE_DESCRIPTOR = "SkipListStore";
    public static final String VERSION = buildTypeDescriptorAndVersion( TYPE_DESCRIPTOR );

    // Governed by h_max = ceil( -(log n_max) / (log p) )
    public static final int P_BITS = 2;  /* p = 1/2^P_BITS */
    public static final int H_MAX  = 18; /* stores up to 2^36 node or relationship ids */

    public static final int BLOCK_SIZE = 88;

    private final RecordFieldSerializer<K> keySerializer;
    private final RecordFieldSerializer<V> valueSerializer;

    public SkipListStore( File fileName, Config conf, IdType idType, IdGeneratorFactory idGeneratorFactory,
                          WindowPoolFactory windowPoolFactory, FileSystemAbstraction fileSystemAbstraction,
                          StringLogger stringLogger,
                          RecordFieldSerializer<K> keySerializer, RecordFieldSerializer<V> valueSerializer )
    {
        super( fileName, conf, idType, idGeneratorFactory, windowPoolFactory, fileSystemAbstraction, stringLogger );
        this.keySerializer = keySerializer;
        this.valueSerializer = valueSerializer;
    }

    @Override
    public String getTypeDescriptor()
    {
        return TYPE_DESCRIPTOR;
    }

    @Override
    public void accept( Processor processor, DynamicRecord record )
    {
        // TODO: Implement
        //
        // fetch key and val and pass that to the processor, ignore head
    }

    @Override
    public SkipListStoreCabinet<K, V> openCabinet( LevelGenerator levelGenerator )
    {
        return new SkipListStoreCabinet<K, V>( levelGenerator, new StoreView() );
    }

    public static final LevelGenerator newDefaultLevelGenerator()
    {
        return new RandomLevelGenerator( H_MAX, P_BITS );
    }

    public static final LevelGenerator newDefaultLevelGenerator( Random random )
    {
        return new RandomLevelGenerator( random, H_MAX, P_BITS );
    }
    
    class StoreView
    {
        private final GrowableByteArray growableBytes = new GrowableByteArray(  );

        SkipListStoreRecord<K, V> getHead( int maxHeight )
        {
            long highId = getHighestPossibleIdInUse();
            if (highId == 0L)
                return checkHeadHeight( createHead( maxHeight ), maxHeight );
            else
                return checkHeadHeight( loadHead(), maxHeight );
        }

        private SkipListStoreRecord<K, V> checkHeadHeight( SkipListStoreRecord<K, V> head, int maxHeight )
        {
            if ( head.getHeight() != maxHeight )
                throw new InvalidRecordException(
                        "Expected head of height: " + maxHeight + " but found: " + head.getHeight() );

            return head;
        }

        private SkipListStoreRecord<K, V> createHead( int height )
        {
            if ( HEAD_ID != nextId() )
                throw
                    new ThisShouldNotHappenError( "Stefan", "Attempt to create new head in non-empty skip list store" );

            return new SkipListStoreRecord<K, V>( new long[ height ], SkipListRecordState.CREATED );
        }

        SkipListStoreRecord<K, V> createRecordWithHeight( int height, K key, V value )
        {
            long id = nextId();
            if ( HEAD_ID == id )
                throw
                    new ThisShouldNotHappenError( "Stefan", "Attempt to recreate skip list head record" );

            return
                new SkipListStoreRecord<K, V>( id, key, value, new long[ height ], SkipListRecordState.CREATED );
        }


        private ByteBuffer serializeRecord( SkipListStoreRecord<K, V> record )
        {
            int requiredLength = computeRequiredLength( record );
            ByteBuffer buffer = growableBytes.getAsWrappedBuffer( requiredLength );
            serializeRecord( record, buffer );
            return buffer;
        }

        private int computeRequiredLength( SkipListStoreRecord<K, V> record )
        {
            if ( record.isHead() )
                return 1 + record.getHeight() * 8;
            else
                return 1 + record.getHeight() * 8
                         + keySerializer.computeSerializedLength( record.getKey() )
                         + valueSerializer.computeSerializedLength( record.getValue() );
        }

        private void serializeRecord( SkipListStoreRecord<K, V> record, ByteBuffer target )
        {
            if ( ! record.isHead() )
            {
                keySerializer.serialize( record.getKey(), target );
                valueSerializer.serialize( record.getValue(), target );
            }
            int height = record.getHeight();
            target.put( (byte) height );
            for (int i = 0; i < height; i++)
                target.putLong( record.getNext( i ) );
        }


        private SkipListStoreRecord<K, V> loadHead()
        {
            Collection<DynamicRecord> dynRecords = loadDynamicRecords( HEAD_ID );
            ByteBuffer dynBuffer = concatData( dynRecords, growableBytes );
            long[] nexts = readNexts( dynBuffer );
            return new SkipListStoreLoadedRecord<K, V>( nexts, dynRecords );
        }

        SkipListStoreRecord<K, V> loadRecord( long id )
        {
            Collection<DynamicRecord> dynRecords = loadDynamicRecords( id );
            ByteBuffer dynBuffer = concatData( dynRecords, growableBytes );
            K key = keySerializer.deSerialize( dynBuffer );
            V value = valueSerializer.deSerialize( dynBuffer );
            long[] nexts = readNexts( dynBuffer );
            return new SkipListStoreLoadedRecord<K, V>( id, key, value, nexts, dynRecords );
        }

        private long[] readNexts( ByteBuffer dynBuffer )
        {
            int height = dynBuffer.get();
            long[] nexts = new long[ height ];
            for ( int i = 0; i < height; i++ )
                nexts[ i ] = dynBuffer.getLong();
            return nexts;
        }

        void storeRecord( SkipListStoreRecord<K, V> record )
        {
            SkipListRecordState dynState = record.getRecordState();
            Collection<DynamicRecord> dynRecords = record.getDynamicRecords();
            if ( dynState.isOutdated )
            {
                if ( SkipListRecordState.REMOVED.equals( dynState ) )
                {
                    if ( dynRecords == null )
                        return;
                    else
                    {
                        for ( DynamicRecord dynRecord : dynRecords )
                            dynRecord.setInUse( false );
                    }
                }
                else
                {
                    if ( dynRecords == null )
                        dynRecords = allocateDynamicRecords( record, asIterator( forceGetRecord( record.getId() ) ) );
                    else
                        dynRecords = updateDynamicRecords( record, dynRecords.iterator() );
                }
            }

            if ( dynState.shouldWrite )
                storeDynamicRecords( dynRecords.iterator() );
        }

        private Collection<DynamicRecord> allocateDynamicRecords( SkipListStoreRecord<K, V> record,
                                                                  Iterator<DynamicRecord> dynRecordSupply )
        {
            ByteBuffer buffer = serializeRecord( record );
            int srcOffset = buffer.arrayOffset();
            int srcLimit = srcOffset + buffer.limit();
            return allocateRecordsFromBytes( buffer.array(), srcOffset, srcLimit, dynRecordSupply );
        }


        private Collection<DynamicRecord> loadDynamicRecords( long id )
        {
            List<DynamicRecord> records = getRecords( id );
            if ( !records.get( 0 ).isStartRecord() )
                throw new IllegalArgumentException( "Invalid start record" );

            return records;
        }

        private Collection<DynamicRecord> updateDynamicRecords( SkipListStoreRecord<K, V> record,
                                                                Iterator<DynamicRecord> dynRecordSupply )
        {
            Collection<DynamicRecord> result = allocateDynamicRecords( record, dynRecordSupply );
            while ( dynRecordSupply.hasNext() )
                dynRecordSupply.next().setInUse( false );
            return result;
        }

        private void storeDynamicRecords( Iterator<DynamicRecord> iterator )
        {
            while ( iterator.hasNext() )
                updateRecord( iterator.next() );
        }
    }

}
