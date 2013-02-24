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

public class InMemSkipListCabinetProvider<K, V> implements SkipListCabinetProvider<InMemSkipListRecord<K, V>, K, V>
{
    public static final int DEFAULT_MAX_HEIGHT = 16;

    private final int defaultMaxHeight;

    public InMemSkipListCabinetProvider()
    {
        this( DEFAULT_MAX_HEIGHT );
    }

    public InMemSkipListCabinetProvider( int defaultMaxHeight )
    {
        this.defaultMaxHeight = defaultMaxHeight;
    }

    @Override
    public SkipListCabinet<InMemSkipListRecord<K, V>, K, V> createRecordOps( int maxHeight )
    {
        return new InMemSkipListCabinet<K, V>( maxHeight );
    }

    @Override
    public SkipListCabinet<InMemSkipListRecord<K, V>, K, V> getDefaultCabinet()
    {
        return createRecordOps( getDefaultMaxHeight() );
    }

    public int getDefaultMaxHeight()
    {
        return defaultMaxHeight;
    }
}
