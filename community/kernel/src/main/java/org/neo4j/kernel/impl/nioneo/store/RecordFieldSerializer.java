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

import java.nio.ByteBuffer;

import org.neo4j.helpers.UTF8;

public interface RecordFieldSerializer<V>
{
    void serialize( V value, ByteBuffer target );

    int computeSerializedLength( V value );

    V deSerialize( ByteBuffer source );

    public static final RecordFieldSerializer<Long> LONG = new RecordFieldSerializer<Long>()
    {
        @Override
        public void serialize( Long value, ByteBuffer target )
        {
            target.putLong( value );
        }

        @Override
        public int computeSerializedLength( Long value )
        {
            return 8;
        }

        @Override
        public Long deSerialize( ByteBuffer source )
        {
            return source.getLong();
        }
    };

    public static final RecordFieldSerializer<String> STRING = new RecordFieldSerializer<String>()
    {

        @Override
        public void serialize( String value, ByteBuffer target )
        {
            byte[] data = UTF8.encode( value );
            target.putInt( data.length );
            target.put( data );
        }

        @Override
        public int computeSerializedLength( String value )
        {
            return 4 + UTF8.encode( value ).length;
        }

        @Override
        public String deSerialize( ByteBuffer source )
        {
            byte[] data = new byte[source.getInt()];
            source.get( data );
            return UTF8.decode( data );
        }
    };
}
