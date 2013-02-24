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

public abstract class AbstractSkipListAccessor<R, K, V>
{
    private final SkipListCabinetProvider<R, K, V> recordOpsProvider;

    public AbstractSkipListAccessor( SkipListCabinetProvider<R, K, V> recordOpsProvider ) {
        this.recordOpsProvider = recordOpsProvider;
    }

    public SkipListCabinet<R, K, V> createNewRecordOps( int maxHeight ) {
        return recordOpsProvider.createRecordOps( maxHeight );
    }

    public SkipListCabinet<R, K, V> getDefaultCabinet() {
        return recordOpsProvider.getDefaultCabinet();
    }

    public int getMaxLevelIndex(SkipListCabinet<R, K, V> context) {
        return context.getMaxHeight();
    }

    public R searchEntry( SkipListCabinet<R, K, V> context, K key, V value, R[] updates ) {
        context = acquireContext( context );
        try
        {
            R entry = context.getHead();
            for(int level = context.getCurrentMaxLevel(); level > 0; level--)
            {
              while (true) {
                  R nextEntry = context.nextAtLevelLessThan( entry, level, key, value );
                  if (context.isNil( nextEntry ) )
                      break;
                  else
                      entry = nextEntry;
              }
              if (updates != null)
                  updates[level - 1] = entry;
            }
            R resultEntry = context.getLowestNext( entry );
            if (  ( context.isNil( resultEntry ) )
               && ( compareKeys( context.getRecordKey( resultEntry ), key ) == 0 )
               && ( compareValues( context.getRecordValue( resultEntry ), value ) == 0 ) )
                return resultEntry;
            else
                return context.nil();
        }
        finally {
            context.release();
        }
    }

    protected SkipListCabinet<R, K, V> acquireContext( SkipListCabinet<R, K, V> context )
    {
        if (context == null)
            return getDefaultCabinet();
        else
        {
            context.acquire();
            return context;
        }
    }

    public abstract int compareKeys(K a, K b);
    public abstract int compareValues(V a, V b);
}
