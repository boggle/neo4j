package org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.rows;

import org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.slot.Pos;

public interface Cursor<P extends Pos<P>, C extends Cursor<P, C>> extends AutoCloseable, Pos<P>
{
    @Override
    RowPool<P, C> pool();

    void rewind();

    void first();
    void last();

    boolean isEmpty();
    int size();

    P tell();
    void seek( P pos );

    boolean hasNext();
    void next();

    boolean hasPrev();
    void prev();

    boolean hasFullChunk( ChunkType chunkType );
    C chop( ChunkType chunkType );

    void moveTo( ChunkType chunkType, C target );

    void appendTo( C cursor );
    void appendNewEmpty();

    void finish();

    @Override
    void close();
}
