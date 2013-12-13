package org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.rows;

import org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.slot.Cursor;

public interface RowPool<C extends Cursor<C>>
{
    C newCursor();
}
