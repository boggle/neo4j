package org.neo4j.kernel.impl.nioneo.store;

import java.nio.ByteBuffer;

public class KeyValueSerialization
{
    public static <K, V> int computeSerializedLength( KeyValueSerializer<K, V> kvSerializer, K key, V value )
    {
        return kvSerializer.getKeySerializer().computeSerializedLength( key )
               + kvSerializer.getValueSerializer().computeSerializedLength( value );
    }

    public static <K, V> void serialize( KeyValueSerializer<K, V> kvSerializer, K key, V value, ByteBuffer target )
    {
        kvSerializer.getKeySerializer().serialize( key, target );
        kvSerializer.getValueSerializer().serialize( value, target );
    }
}
