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
        if ( byteArray == null || byteArray.length < requiredLength )
        {
            byteArray = new byte[ requiredLength ];
        }
        return byteArray;
    }

    public ByteBuffer getAsWrappedBuffer( int requiredLength )
    {
        return ByteBuffer.wrap( get( requiredLength ) );
    }
}
