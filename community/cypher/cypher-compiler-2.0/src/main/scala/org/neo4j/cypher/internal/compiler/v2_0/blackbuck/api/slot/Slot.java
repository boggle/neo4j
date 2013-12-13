package org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.slot;

public interface Slot<C extends Cursor<C>> extends TypedSlot<C>, SlotReader<C>, SlotWriter<C>
{
    void swap( Slot<C> other );
}
