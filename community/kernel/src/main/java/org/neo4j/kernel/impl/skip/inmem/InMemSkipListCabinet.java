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
package org.neo4j.kernel.impl.skip.inmem;

import java.lang.reflect.Array;

import org.neo4j.kernel.impl.skip.LevelGenerator;
import org.neo4j.kernel.impl.skip.SkipListCabinet;
import org.neo4j.kernel.impl.skip.base.SkipListCabinetBase;

class InMemSkipListCabinet<K, V> extends SkipListCabinetBase<InMemSkipListRecord<K, V>, K, V>
{
    private final InMemSkipListRecord<K, V> head;
    private final LevelGenerator levelGenerator;

    InMemSkipListCabinet( LevelGenerator levelGenerator )
    {
        this( levelGenerator, new InMemSkipListRecord<K, V>( levelGenerator.getMaxHeight() ) );
    }

    private InMemSkipListCabinet( LevelGenerator levelGenerator, InMemSkipListRecord<K, V> head )
    {
        super( levelGenerator.getMaxHeight() );
        this.head = head;
        this.levelGenerator = levelGenerator;
    }

    public InMemSkipListRecord<K, V> createRecordWithHeight( int height, K key, V data )
    {
        assertOpen();
        return new InMemSkipListRecord<K, V>( height, key, data );
    }

    @Override
    public void removeRecord( InMemSkipListRecord<K, V> record )
    {
        assertOpen();
        if (isHead( record ))
            throw new IllegalArgumentException( "Attempt to remove head record" );
        else
            /* no-op */
            ;
    }

    @Override
    public boolean areSameRecord( InMemSkipListRecord<K, V> first, InMemSkipListRecord<K, V> second )
    {
        return first == second;
    }

    public K getRecordKey( InMemSkipListRecord<K, V> record )
    {
        assertOpen();
        return record.getKey();
    }

    public V getRecordValue( InMemSkipListRecord<K, V> record )
    {
        assertOpen();
        return record.getValue();
    }

    public int getHeight( InMemSkipListRecord<K, V> record )
    {
        assertOpen();
        return record.nexts.length;
    }

    public InMemSkipListRecord<K, V> getNext( InMemSkipListRecord<K, V> record, int i )
    {
        assertOpen();
        return record.nexts[i];
    }

    public void setNext( InMemSkipListRecord<K, V> record, int i, InMemSkipListRecord<K, V> newNext )
    {
        assertOpen();
        record.nexts[i] = newNext;
    }

    public InMemSkipListRecord<K, V> nil()
    {
        assertOpen();
        return null;
    }

    public boolean isNil( InMemSkipListRecord<K, V> kvEntry )
    {
        assertOpen();
        return kvEntry == null;
    }

    public boolean isHead( InMemSkipListRecord<K, V> kvEntry )
    {
        assertOpen();
        return ! isNil( kvEntry ) && kvEntry.isHead();
    }

    public InMemSkipListRecord<K, V> getHead()
    {
        return head;
    }

    @SuppressWarnings("unchecked")
    @Override
    public InMemSkipListRecord<K, V>[] newVisitationArray()
    {
        return
            (InMemSkipListRecord<K, V>[]) Array.newInstance( InMemSkipListRecord.class, getMaxHeight() );
    }

    @Override
    public int newRandomLevel()
    {
        return levelGenerator.newLevel();
    }

    public void onClose()
    {
    }

    @Override
    public SkipListCabinet<InMemSkipListRecord<K, V>, K, V> reopen()
    {
        close();
        return new InMemSkipListCabinet<K, V>( levelGenerator, head );
    }
}
