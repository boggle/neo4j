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
package org.neo4j.kernel.impl.util;

import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.neo4j.kernel.impl.nioneo.store.DynamicArrayStore;
import org.neo4j.kernel.impl.nioneo.store.PropertyStore;
import org.neo4j.kernel.impl.nioneo.store.PropertyType;
import org.neo4j.kernel.impl.nioneo.store.ShortArray;
import org.neo4j.kernel.impl.transaction.xaframework.LogBuffer;

public abstract class IoPrimitiveUtils
{
    public static String readLengthAndString( ReadableByteChannel channel,
            ByteBuffer buffer ) throws IOException
    {
        Integer length = readInt( channel, buffer );
        return length != null ? readString( channel, buffer, length ) : null;
    }

    public static String readString( ReadableByteChannel channel, ByteBuffer buffer,
            int length ) throws IOException
    {
        char[] chars = new char[length];
        chars = readCharArray( channel, buffer, chars );
        return chars == null ? null : new String( chars );
    }

    public static void write3bLengthAndString( LogBuffer buffer, String string ) throws IOException
    {
        char[] chars = string.toCharArray();
        // 3 bytes to represent the length (4 is a bit overkill)... maybe
        // this space optimization is a bit overkill also :)
        buffer.putShort( (short)chars.length );
        buffer.put( (byte)(chars.length >> 16) );
        buffer.put( chars );
    }

    public static String read3bLengthAndString( ReadableByteChannel channel, ByteBuffer buffer ) throws IOException
    {
        Short lengthShort = readShort( channel, buffer );
        Byte lengthByte = readByte( channel, buffer );
        if ( lengthShort == null || lengthByte == null )
        {
            return null;
        }
        int length = (lengthByte << 16) | lengthShort;
        return readString( channel, buffer, length );
    }

    public static void write2bLengthAndString( LogBuffer buffer, String string ) throws IOException
    {
        char[] chars = string.toCharArray();
        buffer.putShort( (short)chars.length );
        buffer.put( chars );
    }

    public static String read2bLengthAndString( ReadableByteChannel channel, ByteBuffer buffer ) throws IOException
    {
        Short length = readShort( channel, buffer );
        return length == null ? null : readString( channel, buffer, length );
    }

    private static char[] readCharArray( ReadableByteChannel channel,
            ByteBuffer buffer, char[] charArray ) throws IOException
    {
        buffer.clear();
        int charsLeft = charArray.length;
        int maxSize = buffer.capacity() / 2;
        int offset = 0; // offset in chars
        while ( charsLeft > 0 )
        {
            if ( charsLeft > maxSize )
            {
                buffer.limit( maxSize * 2 );
                charsLeft -= maxSize;
            }
            else
            {
                buffer.limit( charsLeft * 2 );
                charsLeft = 0;
            }
            if ( channel.read( buffer ) != buffer.limit() )
            {
                return null;
            }
            buffer.flip();
            int length = buffer.limit() / 2;
            buffer.asCharBuffer().get( charArray, offset, length );
            offset += length;
            buffer.clear();
        }
        return charArray;
    }

    public static boolean readAndFlip( ReadableByteChannel channel, ByteBuffer buffer, int bytes )
            throws IOException
    {
        buffer.clear();
        buffer.limit( bytes );
        int read = channel.read( buffer );
        if ( read < bytes )
        {
            return false;
        }
        buffer.flip();
        return true;
    }

    public static Byte readByte( ReadableByteChannel channel, ByteBuffer buffer ) throws IOException
    {
        return readAndFlip( channel, buffer, 1 ) ? buffer.get() : null;
    }

    public static Short readShort( ReadableByteChannel channel, ByteBuffer buffer ) throws IOException
    {
        return readAndFlip( channel, buffer, 2 ) ? buffer.getShort() : null;
    }

    public static Integer readInt( ReadableByteChannel channel, ByteBuffer buffer ) throws IOException
    {
        return readAndFlip( channel, buffer, 4 ) ? buffer.getInt() : null;
    }

    public static Long readLong( ReadableByteChannel channel, ByteBuffer buffer ) throws IOException
    {
        return readAndFlip( channel, buffer, 8 ) ? buffer.getLong() : null;
    }

    public static Float readFloat( ReadableByteChannel channel, ByteBuffer buffer ) throws IOException
    {
        return readAndFlip( channel, buffer, 4 ) ? buffer.getFloat() : null;
    }

    public static Double readDouble( ReadableByteChannel channel, ByteBuffer buffer ) throws IOException
    {
        return readAndFlip( channel, buffer, 8 ) ? buffer.getDouble() : null;
    }

    public static byte[] readBytes( ReadableByteChannel channel, byte[] array ) throws IOException
    {
        return readBytes( channel, array, array.length );
    }

    public static byte[] readBytes( ReadableByteChannel channel, byte[] array, int bytes ) throws IOException
    {
        return readAndFlip( channel, ByteBuffer.wrap( array ), bytes ) ? array : null;
    }

    public static Map<String, String> readMap( ReadableByteChannel channel, ByteBuffer buffer ) throws IOException
    {
        int size = readInt( channel, buffer );
        Map<String, String> map = new HashMap<>();
        for ( int i = 0; i < size; i++ )
        {
            String key = readLengthAndString( channel, buffer );
            String value = readLengthAndString( channel, buffer );
            if ( key == null || value == null )
            {
                return null;
            }
            map.put( key, value );
        }
        return map;
    }

    public static Map<String, String> read2bMap( ReadableByteChannel channel, ByteBuffer buffer ) throws IOException
    {
        Short size = readShort( channel, buffer );
        if ( size == null )
        {
            return null;
        }
        Map<String, String> map = new HashMap<>();
        for ( int i = 0; i < size; i++ )
        {
            String key = read2bLengthAndString( channel, buffer );
            String value = read2bLengthAndString( channel, buffer );
            if ( key == null || value == null )
            {
                return null;
            }
            map.put( key, value );
        }
        return map;
    }

    public static Object readArray( ReadableByteChannel channel, ByteBuffer buffer ) throws IOException
    {
        int sizeIncHeader = readInt(channel, buffer);

        byte[] data = readBytes( channel, new byte[sizeIncHeader] );

        byte typeId = data[0];
        if ( typeId == PropertyType.STRING.intValue() )
        {
            ByteBuffer headerBuffer = ByteBuffer.wrap( data, 1/*skip the type*/, data.length - 1 );
            int arrayLength = headerBuffer.getInt();
            String[] result = new String[arrayLength];

            ByteBuffer dataBuffer = ByteBuffer.wrap( data, 5, data.length-5 );
            for ( int i = 0; i < arrayLength; i++ )
            {
                int byteLength = dataBuffer.getInt();
                byte[] stringByteArray = new byte[byteLength];
                dataBuffer.get( stringByteArray );
                result[i] = PropertyStore.decodeString( stringByteArray );
            }
            return result;
        }
        else
        {
            ShortArray type = ShortArray.typeOf( typeId );
            int bitsUsedInLastByte = data[1];
            int requiredBits = data[2];
            if ( requiredBits == 0 )
                return type.createEmptyArray();

            byte[] bArray = new byte[data.length - 3];
            ByteBuffer.wrap(data, 3, sizeIncHeader-3).get( bArray );
            Object result;
            if ( type == ShortArray.BYTE && requiredBits == Byte.SIZE )
            {   // Optimization for byte arrays (probably large ones)
                result = bArray;
            }
            else
            {   // Fallback to the generic approach, which is a slower
                Bits bits = Bits.bitsFromBytes( bArray );
                int length = (bArray.length*8-(8-bitsUsedInLastByte))/requiredBits;
                result = type.createArray(length, bits, requiredBits);
            }
            return result;
        }
    }

    public static void writeArray( LogBuffer buffer, Object value ) throws IOException
    {
        byte[] serialized = DynamicArrayStore.serializeArray( value );
        buffer.putInt( serialized.length );
        buffer.put(serialized);
    }

    public static void writeLengthAndString( FileChannel channel, ByteBuffer buffer, String value )
            throws IOException
    {
        char[] chars = value.toCharArray();
        int length = chars.length;
        writeInt( channel, buffer, length );
        writeChars( channel, buffer, chars );
    }

    private static void writeChars( FileChannel channel, ByteBuffer buffer, char[] chars )
            throws IOException
    {
        int position = 0;
        do
        {
            buffer.clear();
            int leftToWrite = chars.length - position;
            if ( leftToWrite * 2 < buffer.capacity() )
            {
                buffer.asCharBuffer().put( chars, position, leftToWrite );
                buffer.limit( leftToWrite * 2);
                channel.write( buffer );
                position += leftToWrite;
            }
            else
            {
                int length = buffer.capacity() / 2;
                buffer.asCharBuffer().put( chars, position, length );
                buffer.limit( length * 2 );
                channel.write( buffer );
                position += length;
            }
        } while ( position < chars.length );
    }

    public static void writeInt( FileChannel channel, ByteBuffer buffer, int value )
            throws IOException
    {
        buffer.clear();
        buffer.putInt( value );
        buffer.flip();
        channel.write( buffer );
    }

    public static Object[] asArray( Object propertyValue )
    {
        if ( propertyValue.getClass().isArray() )
        {
            int length = Array.getLength( propertyValue );
            Object[] result = new Object[ length ];
            for ( int i = 0; i < length; i++ )
            {
                result[ i ] = Array.get( propertyValue, i );
            }
            return result;
        }
        else
        {
            return new Object[] { propertyValue };
        }
    }

    public static Collection<Object> arrayAsCollection( Object arrayValue )
    {
        assert arrayValue.getClass().isArray();

        Collection<Object> result = new ArrayList<>();
        int length = Array.getLength( arrayValue );
        for ( int i = 0; i < length; i++ )
        {
            result.add( Array.get( arrayValue, i ) );
        }
        return result;
    }

    public static int safeCastLongToInt( long value )
    {
        if ( value >= Integer.MAX_VALUE )
        {
            throw new IllegalArgumentException( "Casting long value " + value + " to an int would wrap around" );
        }
        return (int) value;
    }
}
