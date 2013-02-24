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
package org.neo4j.kernel.impl.skip;

class InMemSkipListCabinet<K, V> extends AbstractSkipListCabinet<InMemSkipListRecord<K, V>, K,V>
{
    private final InMemSkipListRecord<K, V> head;

    InMemSkipListCabinet( int maxHeight )
    {
        super( maxHeight );
        this.head = new InMemSkipListRecord<K, V>( maxHeight );
    }

    public InMemSkipListRecord<K, V> createRecord( int height, K key, V data )
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
            throw new UnsupportedOperationException(  );
    }

    @Override
    public boolean areSameRecord( InMemSkipListRecord<K, V> first, InMemSkipListRecord<K, V> second )
    {
        return first == second;
    }

    public K getRecordKey( InMemSkipListRecord<K, V> record )
    {
        assertOpen();
        return record.key;
    }

    public V getRecordValue( InMemSkipListRecord<K, V> record )
    {
        assertOpen();
        return record.value;
    }

    public V setRecordValue( InMemSkipListRecord<K, V> entry, V newData )
    {
        assertOpen();
        V oldData = entry.value;
        entry.value = newData;
        return oldData;
    }

    public int getHeight( InMemSkipListRecord<K, V> record )
    {
        assertOpen();
        return record.next.length;
    }

    public InMemSkipListRecord<K, V> getNext( InMemSkipListRecord<K, V> record, int i )
    {
        assertOpen();
        return record.next[i];
    }

    public void setNext( InMemSkipListRecord<K, V> record, int i, InMemSkipListRecord<K, V> newNext )
    {
        assertOpen();
        record.next[i] = newNext;
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

    @Override
    public InMemSkipListRecord<K, V> getLowestNext( InMemSkipListRecord<K, V> entry )
    {
        throw new UnsupportedOperationException(  );
    }

    @Override
    public InMemSkipListRecord<K, V> nextAtLevelLessThan( InMemSkipListRecord<K, V> record, int level, K key, V value )
    {
        throw new UnsupportedOperationException(  );
    }

    public void finish()
    {
        throw new UnsupportedOperationException(  );
    }

}
