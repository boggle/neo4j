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

public class GrowableByteArray
{
    private byte[] byteArray;

    public GrowableByteArray( int initialLength )
    {
        if ( initialLength <= 0 )
            throw new IllegalArgumentException( "initial length must be > 0" );

        byteArray = new byte[ initialLength ];
    }

    public GrowableByteArray( )
    {
        byteArray = null;
    }

    public byte[] get( int requiredLength )
    {
        grow( requiredLength );
        return byteArray;
    }

    public void grow( int requiredLength )
    {
        if ( requiredLength < 1 )
            throw new IllegalArgumentException( "required length must be > 0 " );

        if ( byteArray == null )
        {
            byteArray = new byte[ requiredLength ];
        }
        else if ( byteArray.length < requiredLength )
        {
            byteArray = new byte[ requiredLength ];
        }
    }

    public int getLength()
    {
        if ( byteArray == null)
            throw new IllegalStateException( "Empty growable byte array does not have a length" );
        return byteArray.length;
    }

    public ByteBuffer getAsWrappedBuffer( int requiredLength )
    {
        return ByteBuffer.wrap( get( requiredLength ) );
    }
}

