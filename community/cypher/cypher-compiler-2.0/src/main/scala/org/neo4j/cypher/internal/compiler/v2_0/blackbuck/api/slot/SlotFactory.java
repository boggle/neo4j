package org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.slot;

import org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.rows.Cursor;

public interface SlotFactory<C extends Pos<C> & Cursor<C>>
{
    Slot<C> newSlot( SlotType type );
    Slot<C> newAnySlot( Object initialValue );
    Slot<C> newIntSlot( int initialValue );
}
