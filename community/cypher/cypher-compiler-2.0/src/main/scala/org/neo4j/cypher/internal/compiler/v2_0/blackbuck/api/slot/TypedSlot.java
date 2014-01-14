package org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.slot;

import org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.rows.Cursor;

public interface TypedSlot<P extends Pos<P>>
{
    SlotType type();
}
