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

import org.neo4j.kernel.impl.skip.base.SkipListCabinetBase;

public abstract class SkipListCabinetBaseAdapter<R, K, V> extends SkipListCabinetBase<R, K, V>
{

    SkipListCabinetBaseAdapter( int maxHeight )
    {
        super( maxHeight );
    }

    @Override
    public R nil()
    {
        return null;
    }

    @Override
    public R getHead()
    {
        throw unsupportedOperation();
    }

    @Override
    public boolean isNil( R record )
    {
        return null == record;
    }

    @Override
    public boolean isHead( R record )
    {
        throw unsupportedOperation();
    }

    @Override
    public int newRandomLevel()
    {
        throw unsupportedOperation();
    }

    @Override
    public R createRecordWithHeight( int height, K key, V value )
    {
        throw unsupportedOperation();
    }

    @Override
    public void removeRecord( R record )
    {
        throw unsupportedOperation();
    }

    @Override
    public boolean areSameRecord( R first, R second )
    {
        return first == second;
    }

    @Override
    public K getRecordKey( R record )
    {
        throw unsupportedOperation();
    }

    private UnsupportedOperationException unsupportedOperation()
    {
        return new UnsupportedOperationException(  );
    }

    @Override
    public V getRecordValue( R record )
    {
        throw unsupportedOperation();
    }

    @Override
    public int getHeight( R record )
    {
        throw unsupportedOperation();
    }

    @Override
    public R getNext( R record, int i )
    {
        return nil();
    }

    @Override
    public void setNext( R record, int i, R newNext )
    {
    }

    @Override
    protected void onClose()
    {
    }
}
