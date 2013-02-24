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

public abstract class AbstractSkipListCabinet<R, K, V> implements SkipListCabinet<R, K, V>
{
    private final int maxHeight;

    private int refCount = 1;

    protected AbstractSkipListCabinet( int maxHeight )
    {
        if (maxHeight < 1)
            throw new IllegalArgumentException(  );

        this.maxHeight = maxHeight;
    }

    @Override
    public void acquire()
    {
        assertOpen();
        refCount += 1;
    }

    @Override
    public void release()
    {
        assertOpen();
        if (refCount == 1)
            close();
        else
            refCount -= 1;
    }

    @Override
    public void close()
    {
        assertOpen();
        try {
            finish();
        }
        finally {
            refCount = 0;
        }
    }

    protected abstract void finish();

    @Override
    public boolean isOpen()
    {
        return refCount > 0;
    }


    @Override
    public int getCurrentMaxLevel()
    {
        return getHeight( getHead() );
    }

    @Override
    public int getMaxHeight()
    {
        return this.maxHeight;
    }


    protected void assertOpen() {
        if (! isOpen())
            throw new IllegalStateException( "Context already closed" );
    }
}
