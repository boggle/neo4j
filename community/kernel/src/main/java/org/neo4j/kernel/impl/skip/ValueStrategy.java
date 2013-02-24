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

import java.nio.ByteBuffer;
import java.util.Comparator;

public interface ValueStrategy<V> extends Comparator<V>
{
    int length( V value );
    V read( ByteBuffer source );
    void append( ByteBuffer target, V value );

    public static final ValueStrategy<Long> LONG = new ValueStrategy<Long>() {
        @Override
        public int length( Long value )
        {
            return 8;
        }

        @Override
        public Long read( ByteBuffer source )
        {
            return source.getLong();
        }

        @Override
        public void append( ByteBuffer target, Long value )
        {
            target.putLong(value);
        }

        @Override
        public int compare( Long o1, Long o2 )
        {
            return Long.signum( o1 - o2 );
        }
    };
}