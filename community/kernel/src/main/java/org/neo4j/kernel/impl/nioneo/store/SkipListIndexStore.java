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
package org.neo4j.kernel.impl.nioneo.store;

import static java.util.Arrays.asList;

import java.io.File;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

import org.neo4j.kernel.IdGeneratorFactory;
import org.neo4j.kernel.IdType;
import org.neo4j.kernel.configuration.Config;
import org.neo4j.kernel.impl.nioneo.store.windowpool.WindowPoolFactory;
import org.neo4j.kernel.impl.skip.ValueStrategy;
import org.neo4j.kernel.impl.util.StringLogger;

/**
 *
 * Tested via LabelStore subclass
 *
 * @param <K> key type
 * @param <V> value type
 *
 * @author Stefan Plantikow
 *
 */
public abstract class SkipListIndexStore<K, V> extends AbstractDynamicStore
{
    // Governed by h_max = ceil( -(log n_max) / (log p) )
    public static final int P_BITS = 2;  /* p = 1/2^P_BITS */
    public static final int H_MAX  = 18; /* stores up to 2^36 node or relationship ips */

    public static final int BLOCK_SIZE = 42;

    public static final long HEAD_ID = 1;

    private final ValueStrategy<K> keyStrategy;
    private final ValueStrategy<V> valueStrategy;

//    private final Head head;
//    private final int numHeadRecords;
//    private final RandomHeightGenerator heightGen;

    private int maxLevelIndex;

    public SkipListIndexStore( File fileName, Config conf, IdType idType, IdGeneratorFactory idGeneratorFactory,
            WindowPoolFactory windowPoolFactory, FileSystemAbstraction fileSystemAbstraction,
            StringLogger stringLogger, ValueStrategy<K> keyStrategy, ValueStrategy<V> valueStrategy)
    {
        super( fileName, conf, idType, idGeneratorFactory, windowPoolFactory, fileSystemAbstraction, stringLogger );
        this.keyStrategy = keyStrategy;
        this.valueStrategy = valueStrategy;
//        this.heightGen = new RandomHeightGenerator(  );
//        this.head = loadOrCreateHead();
//        this.numHeadRecords = editRecords( head.id, head ).size();
//        this.maxLevelIndex = head.getMaxLevelIndex();
    }

    @Override
    public String getTypeDescriptor()
    {
        return null;
    }

    @Override
    public void accept( Processor processor, DynamicRecord record )
    {
    }

    //
//    public int getMaxLevelIndex() {
//        return head.getMaxLevelIndex();
//    }
//
//    public V getFirst( K key ) {
//        Entry entry = searchFirst( key, null );
//        if (entry == null)
//            throw new IllegalArgumentException( key.toString() );
//        return entry.value;
//    }
//
//    public V getFirst( K key, V defaultValue ) {
//        Entry entry = searchFirst( key, null );
//        if (entry == null)
//            return defaultValue;
//        return entry.value;
//    }
//
//    public Iterator<V> getAll( final K key ) {
//        return new Iterator<V>() {
//            Entry entry = searchFirst( key, null );
//
//            @Override
//            public boolean hasNext()
//            {
//                return entry != null;
//            }
//
//            @Override
//            public V next()
//            {
//                V result = entry.value;
//                entry = entry.getLowestNext();
//                if (keyStrategy.compare( key, entry.key ) != 0)
//                    entry = null;
//                return result;
//            }
//
//            @Override
//            public void remove()
//            {
//            }
//        };
//    }
//
//    public boolean contains( K key ) {
//        return searchFirst( key, null ) != null;
//    }
//
//    public boolean contains( K key, V value ) {
//        return searchEntry( key, value, null ) != null;
//    }
//
//    public RandomHeightGenerator getHeightGenerator() {
//        return heightGen;
//    }
//
//    Entry searchFirst( K key, BaseEntry[] updates ) {
//        BaseEntry entry = head;
//        for(int levelIndex = maxLevelIndex; levelIndex >= 0; levelIndex--)
//        {
//            while (true) {
//                Entry nextEntry = entry.nextAtLevelIndexLessThan( levelIndex, key );
//                if (nextEntry == null)
//                    break;
//                else
//                    entry = nextEntry;
//            }
//            if (updates != null)
//                updates[levelIndex] = entry;
//        }
//        Entry resultEntry = entry.getLowestNext();
//
//        if (    (resultEntry != null)
//             && ( keyStrategy.compare( resultEntry.key, key ) == 0 ) )
//            return resultEntry;
//
//        return null;
//    }
//
//
//    Entry searchEntry( K key, V value, BaseEntry[] updates ) {
//        BaseEntry entry = head;
//        for(int levelIndex = maxLevelIndex; levelIndex >= 0; levelIndex--)
//        {
//            while (true) {
//                Entry nextEntry = entry.nextAtLevelIndexLessThan( levelIndex, key, value );
//                if (nextEntry == null)
//                    break;
//                else
//                    entry = nextEntry;
//            }
//            if (updates != null)
//                updates[levelIndex] = entry;
//        }
//        Entry resultEntry = entry.getLowestNext();
//        if (    (resultEntry != null)
//             && ( keyStrategy.compare( resultEntry.key, key ) == 0 )
//             && ( valueStrategy.compare( resultEntry.value, value ) == 0 ) )
//            return resultEntry;
//
//        return null;
//    }
//
//    public void insert( K key, V value ) {
//        // SEARCH (keeping track of insert points per level)
//        BaseEntry[] updates = (BaseEntry[]) Array.newInstance( BaseEntry.class, H_MAX );
//        Entry match = searchEntry( key, value, updates );
//        if (match != null)
//            return;
//        insertMissing( key, value, updates );
//    }
//
//
//    public void delete( K key, V value ) {
//        // SEARCH (keeping track of insert points per level)
//        BaseEntry[] updates = (BaseEntry[]) Array.newInstance( BaseEntry.class, H_MAX );
//        Entry match = searchEntry( key, value, updates );
//        if (match == null)
//            return;
//
//        // UPDATE next pointers
//        Map<Long, BaseEntry> touchedEntries = new HashMap<Long, BaseEntry>(H_MAX);
//        for (int i = 0; i <= maxLevelIndex; i++)
//        {
//            if (updates[i].next[i] != match.id)
//                break;
//            updates[i].next[i] = match.id;
//            touchedEntries.put( updates[i].id, updates[i] );
//        }
//
//        // UPDATE maxLevel
//        for (int i = maxLevelIndex; i > 0; i--) {
//            if (head.next[i] != 0)
//                break;
//        }
//
//        // UPDATE storage
//        storeEntries( touchedEntries.values() );
//        storeRecords( deleteRecords( match.id ) ) ;
//    }
//
//    public void update( K key, V oldValue, V newValue ) {
//        delete( key, oldValue );
//        insert( key, newValue );
//    }
//
//    private void insertMissing( K key, V value, BaseEntry[] updates )
//    {
//        // GENERATE NEW ENTRY
//        int newHeight = getNewHeight( updates );
//        Entry newEntry = new Entry( nextId(), key, value, new long[newHeight] );
//
//        // FIX POINTERS
//        Map<Long, BaseEntry> touchedEntries = new HashMap<Long, BaseEntry>(H_MAX);
//        for (int i = 0; i < newHeight; i++)
//        {
//            newEntry.next[i] = updates[i].next[i];
//            updates[i].next[i] = newEntry.id;
//            touchedEntries.put( updates[i].id, updates[i] );
//        }
//
//        // UPDATE STORAGE
//        storeRecords( allocateRecords( newEntry ) );
//        storeEntries( touchedEntries.values() );
//    }
//
//    private int getNewHeight( BaseEntry[] updates )
//    {
//        int newHeight = heightGen.getRandomHeight();
//        int newMaxLevelIndex = newHeight - 1;
//
//        if (newMaxLevelIndex > maxLevelIndex)
//        {
//            for (int i = newMaxLevelIndex; i > maxLevelIndex; i--)
//                updates[i] = head;
//            maxLevelIndex = newMaxLevelIndex;
//        }
//        return newHeight;
//    }
//
//    private Head loadOrCreateHead() {
//        long highId = getHighestPossibleIdInUse();
//        if (highId == 0L) {
//            Head head = new Head( nextId() );
//            Collection<DynamicRecord> records = allocateRecords( head );
//            storeRecords( records );
//            return head;
//        }
//        else
//            return new Head( HEAD_ID, loadBuffer( HEAD_ID, null ) );
//    }
//
//    private Entry loadEntry( long id )
//    {
//        if (id == HEAD_ID)
//            throw new IllegalArgumentException( "Attempt to load skip list head as entry" );
//        return new Entry( id, loadBuffer( id, null ) );
//    }
//
//    private void storeEntries( Collection<BaseEntry> touchedEntries )
//    {
//        /* heuristic:
//           allocate enough dynamic records for average entry size (1 record assumption) and head record by default
//         */
//        Collection<DynamicRecord> touchedRecords = new ArrayList<DynamicRecord>(touchedEntries.size() + numHeadRecords);
//        for (BaseEntry touchedEntry : touchedEntries)
//            touchedRecords.addAll( editRecords( touchedEntry.id, touchedEntry ) );
//        storeRecords( touchedRecords );
//    }
//
//    private Collection<DynamicRecord> allocateRecords( BaseEntry entry )
//    {
//        RecordSerializer serializer = new RecordSerializer();
//        serializer = serializer.append( entry );
//        byte[] buffer = serializer.serialize();
//        return allocateRecordsFromBytes( buffer, asList( forceGetRecord( entry.id ) ).iterator() );
//    }
//
//    private ByteBuffer loadBuffer( long id, byte[] bytes ) {
//        return concatData( loadRecords( id ), bytes == null ? ShortArray.EMPTY_BYTE_ARRAY : bytes );
//    }
//
//    private Collection<DynamicRecord> loadRecords(long id ) {
//         DynamicRecord record = forceGetRecord( id );
//         if ( !record.inUse() || !record.isStartRecord() )
//            throw new IllegalArgumentException( "Invalid start record" );
//
//        return getRecords( id );
//    }
//
//    private Collection<DynamicRecord> editRecords( long id, RecordSerializable data )
//    {
//        RecordSerializer serializer = new RecordSerializer();
//        serializer = serializer.append( data );
//        byte[] source = serializer.serialize();
//
//        Collection<DynamicRecord> records = loadRecords( id );
//        int offset = 0;
//        for (DynamicRecord record : records)
//        {
//            int length = record.getLength();
//            System.arraycopy( source, offset, record.getData(), 0, length );
//            offset += length;
//        }
//        return records;
//    }
//
//    private Collection<DynamicRecord> deleteRecords(long id)
//    {
//        if (id == HEAD_ID)
//            throw new IllegalArgumentException( "Attempt to delete skip list head" );
//        Collection<DynamicRecord> records = loadRecords( id );
//        for (DynamicRecord record : records)
//            record.setInUse( false );
//        return records;
//    }
//
//    private void storeRecords(Collection<DynamicRecord> records)
//    {
//        for (DynamicRecord record : records)
//        {
//            updateRecord( record );
//        }
//    }
//
//    @Override
//    public void accept( Processor processor, DynamicRecord record )
//    {
//        throw new UnsupportedOperationException(  );
//    }
//
//    private abstract class BaseEntry implements RecordSerializable {
//        final long[] next;
//        final long id;
//
//        private BaseEntry( long id, long[] next )
//        {
//            this.id = id;
//            this.next = next;
//        }
//
//        private BaseEntry( long id, ByteBuffer source )
//        {
//            this.id = id;
//            this.next = new long[source.get()];
//            for (int i = 0; i < next.length; i++)
//                next[i] = source.getLong();
//        }
//
//        public void append( ByteBuffer target )
//        {
//            target.put( (byte) next.length );
//            for ( long ptr : next )
//            {
//                target.putLong( ptr );
//            }
//        }
//
//        public int length() {
//            return 1 + 8 * next.length;
//        }
//
//        public int getMaxLevelIndex() {
//            for (int i = next.length-1; i > 1; i--)
//                if (next[i] != 0)
//                    return i;
//            return 0;
//        }
//
//        public final Entry getLowestNext() {
//            long nextEntryId = next[0];
//            return nextEntryId == 0 ? null : loadEntry( nextEntryId );
//        }
//
//        public final Entry nextAtLevelIndexLessThan( int levelIndex, K key ) {
//            if (levelIndex < next.length)
//            {
//                if (next[levelIndex] != 0) {
//                    Entry forward = loadEntry( next[levelIndex] );
//                    if (keyStrategy.compare( forward.key, key ) < 0)
//                        return forward;
//                }
//            }
//            return null;
//        }
//
//        public final Entry nextAtLevelIndexLessThan( int levelIndex, K key, V value ) {
//            if (levelIndex < next.length)
//            {
//                if (next[levelIndex] != 0) {
//                    Entry forward = loadEntry( next[levelIndex] );
//                    int cmp = keyStrategy.compare( forward.key, key );
//                    if ( cmp < 0 )
//                        return forward;
//                    if ((cmp == 0) && (valueStrategy.compare( forward.value, value ) < 0))
//                        return forward;
//                }
//            }
//            return null;
//        }
//    }
//
//    final public class Head extends BaseEntry
//    {
//
//        private Head( long id )
//        {
//            super( id, new long[H_MAX] );
//            if (id != HEAD_ID)
//                throw new IllegalArgumentException( "Invalid head id" );
//        }
//
//        private Head( long id, ByteBuffer source )
//        {
//            super( id, source );
//            if (id != HEAD_ID)
//                throw new IllegalArgumentException( "Invalid head id" );
//        }
//    }
//
//    final public class Entry extends BaseEntry
//    {
//
//        final K key;
//        final V value;
//
//        private Entry( long id, K key, V value, long[] next )
//        {
//           super( id, next );
//           this.key = key;
//           this.value = value;
//        }
//
//        private Entry( long id, ByteBuffer source ) {
//            super( id, source );
//            this.key = keyStrategy.read( source );
//            this.value = valueStrategy.read( source );
//        }
//
//        @Override
//        public int length()
//        {
//            return super.length() + keyStrategy.length( key ) + valueStrategy.length( value );
//        }
//
//        @Override
//        public void append( ByteBuffer target )
//        {
//            super.append( target );
//            keyStrategy.append( target, key );
//            valueStrategy.append( target, value );
//        }
//    }
//
//    @SuppressWarnings({"ConstantConditions", "PointlessArithmeticExpression"})
//    public static final class RandomHeightGenerator
//    {
//        public static final int RAND_BITS = (H_MAX - 1) * P_BITS;
//
//        private static final int P_MASK = (1 << P_BITS) - 1;
//
//        static {
//            assert (RAND_BITS <= 64);
//        }
//
//        private final Random rand;
//
//        public RandomHeightGenerator( Random rand ) {
//            this.rand = rand;
//        }
//
//        public RandomHeightGenerator()
//        {
//            this( new Random(  ) );
//        }
//
//        public int getRandomHeight() {
//            long randomBits = makeRandomBits( rand );
//
//            int i = 1;
//            do {
//                if ((randomBits & P_MASK) == 0)
//                {
//                    /* coin flip success */
//                    randomBits = (randomBits >> P_BITS);
//                    i++;
//                }
//                else
//                {
//                    /* coin flip fail */
//                    break;
//                }
//            } while (i < H_MAX);
//            return i;
//        }
//
//        private static long makeRandomBits(Random rand) {
//            /* Generate only as many random bits as we need */
//            return (RAND_BITS <= 32) ? (long) rand.nextInt() : rand.nextLong();
//        }
//    }
}

