package org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.rows;

import org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.slot.Pos;

import java.util.Comparator;

public interface RowPool<P extends Pos<P>, C extends Cursor<P, C>>
{
    C newCursor();

    void sort( C cursor, Comparator<P> comparator );
    void sortLimit( C cursor, Comparator<P> comparator, int limit );
}
