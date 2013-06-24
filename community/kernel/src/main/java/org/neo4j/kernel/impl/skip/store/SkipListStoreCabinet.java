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

import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.Map;

import org.neo4j.helpers.ThisShouldNotHappenError;
import org.neo4j.kernel.impl.nioneo.store.InvalidRecordException;
import org.neo4j.kernel.impl.skip.LevelGenerator;
import org.neo4j.kernel.impl.skip.SkipListCabinet;
import org.neo4j.kernel.impl.skip.base.SkipListCabinetBase;

public class SkipListStoreCabinet<K, V> extends SkipListCabinetBase<SkipListStoreRecord<K, V>, K, V>
{
    private final LevelGenerator levelGenerator;

    private final Map<Long, SkipListStoreRecord<K, V>> records = new HashMap<Long, SkipListStoreRecord<K, V>>();
    private final SkipListStore<K, V>.StoreView storeView;
    private final SkipListStoreRecord<K, V> head;

    public SkipListStoreCabinet( LevelGenerator levelGenerator, SkipListStore<K, V>.StoreView storeView )
    {
        super( levelGenerator.getMaxHeight() );
        this.levelGenerator = levelGenerator;
        this.storeView = storeView;
        this.head = storeView.getHead( getMaxHeight() );
    }

    @Override
    protected void onClose()
    {
        // TODO: Find out if we need to iterate in order of ids here
        storeView.storeRecord( head );
        for ( Map.Entry<Long,SkipListStoreRecord<K, V>> entry : records.entrySet() )
            storeView.storeRecord( entry.getValue() );
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
        SkipListStoreRecord<K, V> record = storeView.createRecordWithHeight( height, key, value );
        records.put( record.getId(), record );
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
        return getRecord( record.getNext( level ) );
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
        return new SkipListStoreCabinet<K, V>( levelGenerator, storeView );
    }

    private SkipListStoreRecord<K, V> getRecord( long id )
    {
        if ( 0L == id )
            return nil();

        if ( SkipListStore.HEAD_ID == id )
            throw new ThisShouldNotHappenError( "Stefan", "Attempt to load head via pointer" );

        SkipListStoreRecord<K, V> record = records.get( id );
        if ( record == null )
        {
            record = storeView.loadRecord( id );
            records.put( id, record );
        }

        record.assertNotRemoved();
        return record;
    }
}
