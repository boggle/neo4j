package org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.slot;

public interface SlotFactory<C extends Cursor<C>>
{
    Slot<C> newSlot( SlotType type );
    Slot<C> newAnySlot( Object initialValue );
    Slot<C> newIntSlot( int initialValue );
}
