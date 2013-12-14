package org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.slot;

public interface Cursor<C extends Cursor<C>> extends AutoCloseable
{
    void rewind();
    void last();

    boolean hasNext();
    void next();

    void appendTo( C cursor );
    void appendNewEmpty();

    void finish();

    boolean isLastChunk();
    void setLastChunk( boolean isLastChunk );

    boolean hasChunk();
    C  nextChunk();

    @Override
    void close();
}
