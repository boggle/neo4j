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

import static org.neo4j.helpers.collection.IteratorUtil.asIterator;

import java.io.File;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.neo4j.helpers.ThisShouldNotHappenError;
import org.neo4j.kernel.IdGeneratorFactory;
import org.neo4j.kernel.IdType;
import org.neo4j.kernel.configuration.Config;
import org.neo4j.kernel.impl.nioneo.store.AbstractDynamicStore;
import org.neo4j.kernel.impl.nioneo.store.DynamicRecord;
import org.neo4j.kernel.impl.nioneo.store.FileSystemAbstraction;
import org.neo4j.kernel.impl.nioneo.store.GrowableByteArray;
import org.neo4j.kernel.impl.nioneo.store.InvalidRecordException;
import org.neo4j.kernel.impl.nioneo.store.KeyValueSerializer;
import org.neo4j.kernel.impl.nioneo.store.RecordSerializer;
import org.neo4j.kernel.impl.nioneo.store.RecordFieldSerializer;
import org.neo4j.kernel.impl.nioneo.store.windowpool.WindowPoolFactory;
import org.neo4j.kernel.impl.skip.LevelGenerator;
import org.neo4j.kernel.impl.skip.SkipListCabinet;
import org.neo4j.kernel.impl.skip.SkipListCabinetProvider;
import org.neo4j.kernel.impl.skip.base.RandomLevelGenerator;
import org.neo4j.kernel.impl.skip.base.SkipListCabinetBase;
import org.neo4j.kernel.impl.util.StringLogger;

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

    public static final int BLOCK_SIZE = 42;

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
    public Cabinet openCabinet( LevelGenerator levelGenerator )
    {
        return new Cabinet( levelGenerator, new StoreView() );
    }

    public static final LevelGenerator newDefaultLevelGenerator()
    {
        return new RandomLevelGenerator( H_MAX, P_BITS );
    }

    class StoreView implements KeyValueSerializer<K, V>
    {
        private final GrowableByteArray growableBytes = new GrowableByteArray( 2 * BLOCK_SIZE );

        public RecordFieldSerializer<K> getKeySerializer()
        {
            return keySerializer;
        }

        public RecordFieldSerializer<V> getValueSerializer()
        {
            return valueSerializer;
        }

        Collection<DynamicRecord> allocateDynamicRecords( SkipListStoreRecord<K, V> record )
        {
            RecordSerializer serializer = new RecordSerializer();
            serializer = serializer.append( record );
            byte[] buffer = serializer.serialize( growableBytes );
            return allocateRecordsFromBytes( buffer, asIterator( forceGetRecord( record.getId() ) ) );
        }

        Collection<DynamicRecord> loadDynamicRecords( long id )
        {
            List<DynamicRecord> records = getRecords( id );
            if ( !records.get( 0 ).isStartRecord() )
                throw new IllegalArgumentException( "Invalid start record" );

            return records;
        }

        Collection<DynamicRecord> updateDynamicRecords( SkipListStoreRecord<K, V> record,
                                                        Iterator<DynamicRecord> dynRecordSupply )
        {
            RecordSerializer serializer = new RecordSerializer();
            serializer = serializer.append( record );
            byte[] buffer = serializer.serialize( growableBytes );
            Collection<DynamicRecord> result = allocateRecordsFromBytes( buffer, dynRecordSupply );
            while ( dynRecordSupply.hasNext() )
                dynRecordSupply.next().setInUse( false );
            return result;
        }


        ByteBuffer concatDynamicRecords( Collection<DynamicRecord> records )
        {
            return concatData( records, growableBytes );
        }

        void writeDynamicRecords( Iterator<DynamicRecord> iterator )
        {
            while ( iterator.hasNext() )
                updateRecord( iterator.next() );
        }
    }

    private class Cabinet extends SkipListCabinetBase<SkipListStoreRecord<K, V>, K, V>
    {
        private final LevelGenerator levelGenerator;

        private final Map<Long, SkipListStoreRecord<K, V>> records = new HashMap<Long, SkipListStoreRecord<K, V>>();
        private final StoreView storeView;
        private final SkipListStoreRecord<K, V> head;

        public Cabinet( LevelGenerator levelGenerator, StoreView storeView )
        {
            super( levelGenerator.getMaxHeight() );
            this.levelGenerator = levelGenerator;
            this.storeView = storeView;

            int maxHeight = getMaxHeight();
            long highId = getHighestPossibleIdInUse();
            if (highId == 0L)
                head = new SkipListStoreRecord<K, V>( storeView, nextId(), maxHeight );
            else
                head = loadRecordOrHead( HEAD_ID );

            int height = head.getHeight();
            if ( height != maxHeight )
                throw new InvalidRecordException(
                        "Expected head of height: " + maxHeight + " but found: " + height );

            if ( ! head.isHead() )
                throw new ThisShouldNotHappenError( "Stefan", "new skip list head is not a head record" );
        }

        @Override
        protected void onClose()
        {
            // TODO: Find out if we need to iterate in order of ids here
            head.write();
            for ( SkipListStoreRecord<K, V> record : records.values() )
                record.write();
        }

        @SuppressWarnings("unchecked")
        @Override
        public SkipListStoreRecord<K, V>[] newVisitationArray()
        {
            return (SkipListStoreRecord<K, V>[]) Array.newInstance( SkipListStoreRecord.class, getMaxHeight() );
        }

        @Override
        public SkipListStoreRecord<K, V> nil()
        {
            return null;
        }

        @Override
        public SkipListStoreRecord<K, V> getHead()
        {
            assertOpen();
            return head;
        }

        @Override
        public boolean isNil( SkipListStoreRecord<K, V> record )
        {
            return null == record;
        }

        @Override
        public boolean isHead( SkipListStoreRecord<K, V> record )
        {
            assertOpen();
            return ! isNil( record ) && record.isHead();
        }

        @Override
        public int newRandomLevel()
        {
            return levelGenerator.newLevel();
        }

        @Override
        public SkipListStoreRecord<K, V> createRecordWithHeight( int height, K key, V value )
        {
            assertOpen();
            long id = nextId();
            SkipListStoreRecord<K, V> record = new SkipListStoreRecord<K, V>( storeView, id, height, key, value );
            records.put( id, record );
            return record;
        }

        @Override
        public void removeRecord( SkipListStoreRecord<K, V> record )
        {
            assertOpen();
            if ( isNil( record ) )
                throw new InvalidRecordException( "Cannot remove nil()" );
            else
                record.setRemoved();
        }

        @Override
        public boolean areSameRecord( SkipListStoreRecord<K, V> first, SkipListStoreRecord<K, V> second )
        {
            if ( first == second )
                return true;
            if ( first == null )
                return false;
            if ( second == null )
                return false;
            return first.getId() == second.getId();
        }

        @Override
        public K getRecordKey( SkipListStoreRecord<K, V> record )
        {
            assertOpen();
            return record.getKey();
        }

        @Override
        public V getRecordValue( SkipListStoreRecord<K, V> record )
        {
            assertOpen();
            return record.getValue();
        }

        @Override
        public int getHeight( SkipListStoreRecord<K, V> record )
        {
            assertOpen();
            return record.getHeight();
        }

        @Override
        public SkipListStoreRecord<K, V> getNext( SkipListStoreRecord<K, V> record, int level )
        {
            assertOpen();
            return loadRecord( record.getNext( level) );
        }

        @Override
        public void setNext( SkipListStoreRecord<K, V> record, int level, SkipListStoreRecord<K, V> newNext )
        {
            assertOpen();
            if ( isNil( newNext ))
                record.setNext( level, 0 );
            else
                record.setNext( level, newNext.getId() );
        }

        @Override
        public SkipListCabinet<SkipListStoreRecord<K, V>, K, V> reopen()
        {
            close();
            return new Cabinet( levelGenerator, storeView );
        }

        private SkipListStoreRecord<K, V> loadRecord( long id )
        {
            if ( 0L == id )
                return nil();

            if ( HEAD_ID == id )
                throw new ThisShouldNotHappenError( "Stefan", "Attempt to load head via pointer" );

            return loadRecordOrHead( id );
        }

        private SkipListStoreRecord<K, V> loadRecordOrHead( long id )
        {
            SkipListStoreRecord<K, V> record = records.get( id );
            if ( record == null )
            {
                Collection<DynamicRecord> dynRecords = storeView.loadDynamicRecords( id );
                ByteBuffer dynBuffer = storeView.concatDynamicRecords( dynRecords );
                record = new SkipListStoreRecord<K, V>( storeView, id, dynBuffer, dynRecords );
                records.put( id, record );
            }

            record.assertNotRemoved();
            return record;
        }
    }
}
