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

import static java.lang.reflect.Array.newInstance;

import org.neo4j.kernel.impl.skip.base.SkipListRecordBase;

public class InMemSkipListRecord<K, V> extends SkipListRecordBase<K, V>
{
    public final InMemSkipListRecord<K, V>[] nexts;

    InMemSkipListRecord( int height )
    {
        this( height, null, null );
    }

    @SuppressWarnings("unchecked")
    InMemSkipListRecord( int height, K key, V value )
    {
        super(key, value);
        this.nexts = (InMemSkipListRecord<K, V>[]) newInstance( InMemSkipListRecord.class, height );
    }

    @Override
    public int getHeight()
    {
        return nexts.length;
    }
}
