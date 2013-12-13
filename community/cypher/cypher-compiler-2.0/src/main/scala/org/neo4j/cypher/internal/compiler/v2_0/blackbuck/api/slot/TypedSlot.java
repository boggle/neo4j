package org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.slot;

public interface TypedSlot<C extends Cursor<C>>
{
    SlotType type();
}
