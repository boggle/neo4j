package org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.slot;

public interface Slot<P extends Pos<P>> extends TypedSlot<P>, SlotReader<P>, SlotWriter<P>
{
    void swap( P other );
}
