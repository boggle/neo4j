package org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.slot;

public interface Cursor<C extends Cursor<C>> extends AutoCloseable
{
    void rewind();

    boolean hasNext();
    void next();

    C flush();

    void copyTo( C cursor );
    void appendTo( C cursor );
    void appendNewEmpty();

    @Override
    void close();
}
