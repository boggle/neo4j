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

